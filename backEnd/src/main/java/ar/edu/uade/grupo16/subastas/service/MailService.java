package ar.edu.uade.grupo16.subastas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:subastas-g16@ejemplo.com}")
    private String fromEmail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailAprobacion(String destinatario, String nombreUsuario, String categoria) {
        String asunto = "¡Tu cuenta en Subastas G16 fue aprobada!";
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                "¡Buenas noticias! Tu cuenta ha sido aprobada exitosamente.\n\n" +
                "Categoría asignada: %s\n\n" +
                "Ya podés ingresar a la app con tus credenciales y comenzar a participar en subastas.\n" +
                "Recordá registrar al menos un medio de pago antes de pujar.\n\n" +
                "¡Bienvenido al mundo de las subastas!\n\n" +
                "— Equipo Subastas G16",
                nombreUsuario, categoria.toUpperCase()
        );
        enviarEmail(destinatario, asunto, cuerpo);
    }

    public void enviarEmailRechazo(String destinatario, String nombreUsuario) {
        String asunto = "Información sobre tu registro en Subastas G16";
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                "Lamentamos informarte que tu solicitud de registro no ha sido aprobada.\n\n" +
                "Si considerás que esto es un error, podés comunicarte con nuestro soporte.\n\n" +
                "— Equipo Subastas G16",
                nombreUsuario
        );
        enviarEmail(destinatario, asunto, cuerpo);
    }

    public void enviarEmailPujaGanada(String destinatario, String nombreUsuario,
                                       String nombreProducto, String importe, String comision) {
        String asunto = "¡Ganaste una subasta en Subastas G16!";
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                "¡Felicitaciones! Ganaste la puja por: %s\n\n" +
                "Importe pujado: $%s\n" +
                "Comisión: $%s\n\n" +
                "Revisá tu app para ver los detalles del pago y el envío.\n\n" +
                "— Equipo Subastas G16",
                nombreUsuario, nombreProducto, importe, comision
        );
        enviarEmail(destinatario, asunto, cuerpo);
    }

    private void enviarEmail(String destinatario, String asunto, String cuerpo) {
        if (!mailEnabled) {
            log.info("=== EMAIL SIMULADO ===");
            log.info("Para: {}", destinatario);
            log.info("Asunto: {}", asunto);
            log.info("Cuerpo: {}", cuerpo);
            log.info("======================");
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(fromEmail);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Email enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", destinatario, e.getMessage());
        }
    }
}
