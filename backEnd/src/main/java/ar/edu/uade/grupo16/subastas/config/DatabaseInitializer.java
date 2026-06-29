package ar.edu.uade.grupo16.subastas.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando verificación/inicialización de base de datos...");

        // 1. Inicializar países si está vacío
        Integer countPaises = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM paises", Integer.class);
        if (countPaises == null || countPaises == 0) {
            log.info("Tabla 'paises' vacía. Insertando países por defecto...");
            jdbcTemplate.execute("INSERT INTO paises (numero, descripcion) VALUES " +
                    "(1, 'Argentina'), " +
                    "(2, 'Brasil'), " +
                    "(3, 'Uruguay'), " +
                    "(4, 'Chile'), " +
                    "(5, 'Paraguay'), " +
                    "(6, 'Bolivia'), " +
                    "(7, 'Perú'), " +
                    "(8, 'Colombia'), " +
                    "(9, 'México'), " +
                    "(10, 'España'), " +
                    "(11, 'Estados Unidos'), " +
                    "(12, 'Alemania'), " +
                    "(13, 'Francia'), " +
                    "(14, 'Italia'), " +
                    "(15, 'Reino Unido')");
            log.info("Países insertados con éxito.");
        }

        // Desactivar FK checks para inserciones de IDs fijos y reactivarlos al final
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        try {
            // 2. Inicializar persona Administrador (ID 999)
            Integer countAdminPersona = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM personas WHERE identificador = 999", Integer.class);
            if (countAdminPersona == null || countAdminPersona == 0) {
                log.info("Insertando persona administrador (ID 999)...");
                jdbcTemplate.update("INSERT INTO personas (identificador, documento, nombre, direccion, estado, pais) " +
                        "VALUES (999, 'SISTEMA', 'Empleado Sistema', 'Sistema interno', 'activo', 1)");
            }

            // 3. Inicializar empleado Administrador (ID 999)
            Integer countAdminEmpleado = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM empleados WHERE identificador = 999", Integer.class);
            if (countAdminEmpleado == null || countAdminEmpleado == 0) {
                log.info("Insertando empleado administrador (ID 999)...");
                jdbcTemplate.update("INSERT INTO empleados (identificador, cargo, sector) VALUES (999, 'ADMINISTRADOR', NULL)");
            }

            // 4. Inicializar cliente Administrador (ID 999)
            Integer countAdminCliente = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clientes WHERE identificador = 999", Integer.class);
            if (countAdminCliente == null || countAdminCliente == 0) {
                log.info("Insertando cliente administrador (ID 999)...");
                jdbcTemplate.update("INSERT INTO clientes (identificador, pais, admitido, categoria, verificador) " +
                        "VALUES (999, 1, 'si', 'platino', 999)");
            }

            // 5. Inicializar usuario de autenticación Administrador (ID 999 / email admin@gmail.com / pwd admin)
            Integer countAdminAuth = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios_auth WHERE email = 'admin@gmail.com'", Integer.class);
            if (countAdminAuth == null || countAdminAuth == 0) {
                log.info("Insertando usuario_auth administrador (email: admin@gmail.com, password: admin)...");
                String passwordHash = passwordEncoder.encode("admin");
                String uuid = "c3d4e5f6-a789-01bc-def1-234567890abc";
                jdbcTemplate.update("INSERT INTO usuarios_auth (id, persona_id, email, password_hash, estado, uuid, fecha_registro) " +
                        "VALUES (999, 999, 'admin@gmail.com', ?, 'APROBADO', ?, CURRENT_TIMESTAMP)", passwordHash, uuid);
            }

            // 6. Inicializar persona Blackwood Subastas (ID 9999)
            Integer countBlackwoodPersona = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM personas WHERE identificador = 9999", Integer.class);
            if (countBlackwoodPersona == null || countBlackwoodPersona == 0) {
                log.info("Insertando persona Blackwood Subastas (ID 9999)...");
                jdbcTemplate.update("INSERT INTO personas (identificador, documento, nombre, direccion, estado, pais) " +
                        "VALUES (9999, 'BLACKWOOD_SUBASTAS', 'Blackwood Subastas', 'Sede Central', 'activo', 1)");
            }

            // 7. Inicializar cliente Blackwood Subastas (ID 9999)
            Integer countBlackwoodCliente = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clientes WHERE identificador = 9999", Integer.class);
            if (countBlackwoodCliente == null || countBlackwoodCliente == 0) {
                log.info("Insertando cliente Blackwood Subastas (ID 9999)...");
                jdbcTemplate.update("INSERT INTO clientes (identificador, pais, admitido, categoria, verificador) " +
                        "VALUES (9999, 1, 'si', 'platino', 999)");
            }

            log.info("Inicialización de datos completada con éxito.");
        } catch (Exception e) {
            log.error("Error al inicializar los datos de la base de datos: {}", e.getMessage(), e);
            throw e;
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}
