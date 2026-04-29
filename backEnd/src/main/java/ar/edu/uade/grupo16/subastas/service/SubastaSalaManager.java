package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.dto.request.PujaRequest;
import ar.edu.uade.grupo16.subastas.dto.response.PujaResponse;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Gestor de salas de subasta en vivo.
 * Cada subasta tiene una BlockingQueue propia y un único worker thread
 * que procesa las pujas en orden FIFO, garantizando atomicidad sin contention.
 *
 * Arquitectura:
 *   Android client → POST /api/pujas  → cola de la subasta
 *   Worker thread  → Procesa puja     → Broadcast /topic/subasta/{id}
 */
@Component
public class SubastaSalaManager {

    private static final Logger log = LoggerFactory.getLogger(SubastaSalaManager.class);

    // subastaId → cola de solicitudes de puja pendientes
    private final Map<Integer, BlockingQueue<PujaTask>> colas = new ConcurrentHashMap<>();

    // subastaId → worker thread único
    private final Map<Integer, ExecutorService> workers = new ConcurrentHashMap<>();

    private final PujaService pujaService;
    private final SimpMessagingTemplate messagingTemplate;

    public SubastaSalaManager(PujaService pujaService, SimpMessagingTemplate messagingTemplate) {
        this.pujaService = pujaService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Encola una solicitud de puja para la sala.
     * El worker la procesará en orden FIFO.
     */
    public void encolarPuja(Integer subastaId, PujaRequest request, String emailPostor) {
        BlockingQueue<PujaTask> cola = colas.computeIfAbsent(subastaId,
                id -> new LinkedBlockingQueue<>(500));

        ExecutorService worker = workers.computeIfAbsent(subastaId,
                id -> Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "puja-worker-subasta-" + id);
                    t.setDaemon(true);
                    return t;
                }));

        PujaTask task = new PujaTask(subastaId, request, emailPostor);
        boolean encolado = cola.offer(task);

        if (encolado) {
            worker.submit(() -> procesarTarea(cola));
        } else {
            log.warn("Cola llena para subasta {}. Puja descartada de: {}", subastaId, emailPostor);
            // Notificar error al cliente específico
            messagingTemplate.convertAndSendToUser(
                emailPostor,
                "/queue/errores",
                Map.of("error", "Sala saturada, intentá nuevamente en unos segundos")
            );
        }
    }

    private void procesarTarea(BlockingQueue<PujaTask> cola) {
        PujaTask task = cola.poll();
        if (task == null) return;

        try {
            PujaResponse resultado = pujaService.procesarPuja(
                    task.subastaId(), task.request(), task.emailPostor());

            // Broadcast a todos los asistentes de la subasta
            messagingTemplate.convertAndSend(
                    "/topic/subasta/" + task.subastaId(),
                    resultado
            );

            log.info("Puja procesada — Subasta {} | Postor: {} | Importe: {}",
                    task.subastaId(), task.emailPostor(), task.request().getImporte());

        } catch (Exception e) {
            log.error("Error procesando puja en subasta {}: {}", task.subastaId(), e.getMessage());
            // Notificar el error solo al postor que mandó la puja inválida
            messagingTemplate.convertAndSendToUser(
                    task.emailPostor(),
                    "/queue/errores",
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Limpia la sala cuando la subasta cierra.
     */
    public void cerrarSala(Integer subastaId) {
        colas.remove(subastaId);
        ExecutorService worker = workers.remove(subastaId);
        if (worker != null) {
            worker.shutdown();
        }
        log.info("Sala cerrada para subasta {}", subastaId);
    }

    @PreDestroy
    public void shutdown() {
        workers.values().forEach(ExecutorService::shutdownNow);
    }

    // Record inmutable para pasar datos a la cola
    private record PujaTask(Integer subastaId, PujaRequest request, String emailPostor) {}
}
