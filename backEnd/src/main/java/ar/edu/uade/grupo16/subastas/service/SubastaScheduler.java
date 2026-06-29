package ar.edu.uade.grupo16.subastas.service;

import ar.edu.uade.grupo16.subastas.entity.*;
import ar.edu.uade.grupo16.subastas.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubastaScheduler {

    private final SubastaRepository subastaRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final ClienteRepository clienteRepository;
    private final AsistenteRepository asistenteRepository;
    private final PujoRepository pujoRepository;
    private final PujaService pujaService;
    private final UsuarioAuthRepository usuarioAuthRepository;

    /**
     * Tarea 1: Activar subastas programadas que ya llegaron a su hora de inicio.
     * Se ejecuta cada 10 segundos.
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void activarSubastas() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<Subasta> programadas = subastaRepository.findAll().stream()
                .filter(s -> "programada".equalsIgnoreCase(s.getEstado()))
                .toList();

        for (Subasta subasta : programadas) {
            boolean debeIniciar = false;
            if (subasta.getFecha().isBefore(hoy)) {
                debeIniciar = true;
            } else if (subasta.getFecha().isEqual(hoy) && !subasta.getHora().isAfter(ahora)) {
                debeIniciar = true;
            }

            if (debeIniciar) {
                log.info("Activando subasta ID {} programada para {} {}", subasta.getIdentificador(), subasta.getFecha(), subasta.getHora());
                try {
                    activarSubastaInterno(subasta);
                } catch (Exception e) {
                    log.error("Error al activar subasta ID {}: {}", subasta.getIdentificador(), e.getMessage(), e);
                }
            }
        }
    }

    private void activarSubastaInterno(Subasta subasta) {
        // 1. Obtener cliente Blackwood Subastas (ID 9999 o documento BLACKWOOD_SUBASTAS)
        Cliente blackwood = clienteRepository.findById(9999)
                .orElseGet(() -> clienteRepository.findByPersonaDocumento("BLACKWOOD_SUBASTAS")
                        .orElseThrow(() -> new IllegalStateException("Cliente 'Blackwood Subastas' no encontrado en el sistema. Debe ejecutarse el seed de base de datos.")));

        // 2. Registrar como Asistente en la subasta si no existe
        Asistente asistente = asistenteRepository
                .findByClienteIdentificadorAndSubastaIdentificador(blackwood.getIdentificador(), subasta.getIdentificador())
                .orElseGet(() -> {
                    Asistente nuevoAsistente = Asistente.builder()
                            .cliente(blackwood)
                            .subasta(subasta)
                            .numeroPostor(999)
                            .build();
                    return asistenteRepository.save(nuevoAsistente);
                });

        // 3. Registrar Puja Original para cada item del catálogo de la subasta
        List<ItemCatalogo> items = itemCatalogoRepository.findAll().stream()
                .filter(item -> item.getCatalogo() != null && item.getCatalogo().getSubasta() != null
                        && item.getCatalogo().getSubasta().getIdentificador().equals(subasta.getIdentificador()))
                .toList();

        Integer primerItemId = null;
        for (ItemCatalogo item : items) {
            // Registrar pujo inicial si no hay pujas aún
            boolean tienePujas = pujoRepository.findMejorPujaByItem(item.getIdentificador()).isPresent();
            if (!tienePujas) {
                Pujo pujaInicial = Pujo.builder()
                        .asistente(asistente)
                        .item(item)
                        .importe(item.getPrecioBase())
                        .ganador("si")
                        .fechaHora(LocalDateTime.now())
                        .build();
                pujoRepository.save(pujaInicial);
                log.info("Registrada puja inicial (Blackwood Subastas) de ${} para el Item ID {}", item.getPrecioBase(), item.getIdentificador());
            }

            if (item.getOrden() != null && item.getOrden() == 1) {
                primerItemId = item.getIdentificador();
            } else if (primerItemId == null) {
                primerItemId = item.getIdentificador();
            }
        }

        // 4. Cambiar estado y configurar tiempo límite inicial de 5 minutos
        subasta.setEstado("abierta");
        subasta.setItemActualId(primerItemId);
        subasta.setLimiteFinalizacionEpoch(Instant.now().toEpochMilli() + 300000); // 5 minutos

        subastaRepository.save(subasta);
        log.info("Subasta ID {} activada con éxito. Expira en 5 minutos.", subasta.getIdentificador());
    }

    /**
     * Tarea 2: Manejar la expiración del lote actual. Si vence el tiempo (5 minutos),
     * se cierra el lote actual y se pasa al siguiente. Si no hay más lotes, se finaliza la subasta.
     * Se ejecuta cada 10 segundos.
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void finalizarSubastasExpiradas() {
        long ahora = Instant.now().toEpochMilli();

        List<Subasta> abiertas = subastaRepository.findAll().stream()
                .filter(s -> "abierta".equalsIgnoreCase(s.getEstado()))
                .toList();

        for (Subasta subasta : abiertas) {
            if (subasta.getLimiteFinalizacionEpoch() != null && subasta.getLimiteFinalizacionEpoch() <= ahora) {
                log.info("Lote/Subasta ID {} expiró (tiempo: {} <= ahora: {}). Procesando siguiente paso.",
                        subasta.getIdentificador(), subasta.getLimiteFinalizacionEpoch(), ahora);
                try {
                    procesarVencimientoLote(subasta);
                } catch (Exception e) {
                    log.error("Error al procesar vencimiento de lote para subasta ID {}: {}", subasta.getIdentificador(), e.getMessage(), e);
                }
            }
        }
    }

    private void procesarVencimientoLote(Subasta subasta) {
        Integer itemActualId = subasta.getItemActualId();

        // 1. Obtener el email del subastador responsable para la auditoría de cierre
        String emailSubastador = "admin@gmail.com";
        if (subasta.getSubastador() != null && subasta.getSubastador().getPersona() != null) {
            emailSubastador = usuarioAuthRepository.findByPersonaIdentificador(subasta.getSubastador().getPersona().getIdentificador())
                    .map(UsuarioAuth::getEmail)
                    .orElse("admin@gmail.com");
        }

        // 2. Cerrar el item actual si existe y no está subastado
        if (itemActualId != null) {
            Optional<ItemCatalogo> itemOpt = itemCatalogoRepository.findById(itemActualId);
            if (itemOpt.isPresent()) {
                ItemCatalogo itemActual = itemOpt.get();
                if (!"si".equalsIgnoreCase(itemActual.getSubastado())) {
                    try {
                        pujaService.cerrarItem(subasta.getIdentificador(), itemActual.getIdentificador(), emailSubastador);
                        log.info("Lote actual ID {} cerrado/vendido.", itemActual.getIdentificador());
                    } catch (Exception e) {
                        log.error("Error al cerrar Item ID {} de forma automática: {}", itemActual.getIdentificador(), e.getMessage());
                    }
                }
            }
        }

        // 3. Buscar todos los items de la subasta ordenados por su orden/identificador
        List<ItemCatalogo> items = itemCatalogoRepository.findAll().stream()
                .filter(item -> item.getCatalogo() != null && item.getCatalogo().getSubasta() != null
                        && item.getCatalogo().getSubasta().getIdentificador().equals(subasta.getIdentificador()))
                .sorted((a, b) -> {
                    int ordenA = a.getOrden() != null ? a.getOrden() : a.getIdentificador();
                    int ordenB = b.getOrden() != null ? b.getOrden() : b.getIdentificador();
                    return Integer.compare(ordenA, ordenB);
                })
                .toList();

        // 4. Encontrar el siguiente item (primer item no subastado)
        ItemCatalogo siguienteItem = null;
        for (ItemCatalogo item : items) {
            if (!"si".equalsIgnoreCase(item.getSubastado())) {
                siguienteItem = item;
                break;
            }
        }

        if (siguienteItem != null) {
            // --- Caso A: Hay un lote siguiente disponible ---
            log.info("Pasando al siguiente lote ID {} en la subasta ID {}", siguienteItem.getIdentificador(), subasta.getIdentificador());

            // Registrar puja inicial de Blackwood si no tiene
            Cliente blackwood = clienteRepository.findById(9999)
                    .orElseThrow(() -> new IllegalStateException("Cliente 'Blackwood Subastas' no encontrado en el sistema."));

            Asistente asistente = asistenteRepository
                    .findByClienteIdentificadorAndSubastaIdentificador(blackwood.getIdentificador(), subasta.getIdentificador())
                    .orElseGet(() -> {
                        Asistente nuevoAsistente = Asistente.builder()
                                .cliente(blackwood)
                                .subasta(subasta)
                                .numeroPostor(999)
                                .build();
                        return asistenteRepository.save(nuevoAsistente);
                    });

            boolean tienePujas = pujoRepository.findMejorPujaByItem(siguienteItem.getIdentificador()).isPresent();
            if (!tienePujas) {
                Pujo pujaInicial = Pujo.builder()
                        .asistente(asistente)
                        .item(siguienteItem)
                        .importe(siguienteItem.getPrecioBase())
                        .ganador("si")
                        .fechaHora(LocalDateTime.now())
                        .build();
                pujoRepository.save(pujaInicial);
                log.info("Registrada puja inicial (Blackwood Subastas) de ${} para el siguiente Item ID {}", siguienteItem.getPrecioBase(), siguienteItem.getIdentificador());
            }

            subasta.setItemActualId(siguienteItem.getIdentificador());
            subasta.setLimiteFinalizacionEpoch(Instant.now().toEpochMilli() + 300000); // Reiniciar 5 minutos para el nuevo lote
            subastaRepository.save(subasta);
        } else {
            // --- Caso B: No hay más lotes, finaliza toda la subasta ---
            log.info("No hay más lotes. Finalizando subasta ID {} completa.", subasta.getIdentificador());
            subasta.setEstado("cerrada");
            subasta.setItemActualId(null);
            subasta.setLimiteFinalizacionEpoch(null);
            subastaRepository.save(subasta);
        }
    }
}
