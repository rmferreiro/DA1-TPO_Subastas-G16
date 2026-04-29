-- ============================================================
-- SUBASTAS G16 — Datos de prueba completos
-- Ejecutar DESPUÉS de 01_schema_migration.sql
-- ============================================================

USE subastas_bd;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- PAÍSES
-- ============================================================
INSERT INTO paises (identificador, descripcion) VALUES
(1,  'Argentina'),
(2,  'Brasil'),
(3,  'Uruguay'),
(4,  'Chile'),
(5,  'Paraguay'),
(6,  'Bolivia'),
(7,  'Perú'),
(8,  'Colombia'),
(9,  'México'),
(10, 'España'),
(11, 'Estados Unidos'),
(12, 'Alemania'),
(13, 'Francia'),
(14, 'Italia'),
(15, 'Reino Unido');

-- ============================================================
-- PERSONAS BASE (clientes, empleados, subastadores, dueños)
-- ============================================================
INSERT INTO personas (identificador, nombre, documento, direccion, pais) VALUES
-- Clientes
(1,  'Juan Pablo Rodríguez',    '30111222', 'Av. Corrientes 1234, CABA',        1),
(2,  'María Laura González',    '28333444', 'Av. Santa Fe 567, CABA',           1),
(3,  'Carlos Alberto Martínez', '25555666', 'Calle 9 de Julio 890, Rosario',    1),
(4,  'Ana Carolina López',      '33777888', 'Mitre 234, Córdoba',               1),
(5,  'Roberto Sebastián Pérez', '27999000', 'España 456, Mendoza',              1),
(6,  'Valentina Torres',        '35111222', 'Brasil 789, Buenos Aires',         1),
(7,  'Diego Alejandro Sánchez', '29333444', 'Defensa 101, San Telmo',           1),
(8,  'Laura Silvina Moreno',    '31555666', 'Armenia 222, Palermo',             1),
-- Empleados
(10, 'Fernando García',         '26777888', 'Av. de Mayo 1000, CABA',           1),
(11, 'Silvia Rojas',            '24999000', 'Rivadavia 500, CABA',              1),
-- Subastadores
(20, 'Licenciado Andrés Castro','23111222', 'Florida 123, CABA',                1),
(21, 'Doctora Beatriz Vargas',  '22333444', 'Marcelo T. Alvear 456, CABA',     1),
-- Dueños de bienes
(30, 'Galería de Arte Borges',  '30444555', 'Recoleta 77, CABA',               1),
(31, 'Casa Subastas del Sur',   '28666777', 'San Martín 333, Buenos Aires',    1),
(32, 'Colección Privada Smith', '99888777', '5th Avenue 100, New York',        11);

-- ============================================================
-- CLIENTES
-- ============================================================
INSERT INTO clientes (identificador, pais, categoria, admitido) VALUES
(1, 1, 'comun',        1),  -- Juan Pablo - NORMAL
(2, 1, 'plata',        1),  -- María Laura - SILVER  
(3, 1, 'oro',          1),  -- Carlos - GOLD
(4, 1, 'comun',        1),  -- Ana Carolina
(5, 1, 'plata',        1),  -- Roberto
(6, 1, 'comun',        0),  -- Valentina - pendiente de aprobación
(7, 1, 'oro',          1),  -- Diego - GOLD
(8, 1, 'platino',      1);  -- Laura - PLATINUM (mayor nivel)

-- ============================================================
-- EMPLEADOS
-- ============================================================
INSERT INTO empleados (identificador, legajo, sector) VALUES
(10, 'EMP-001', 'Revisión de Productos'),
(11, 'EMP-002', 'Atención al Cliente');

-- ============================================================
-- SUBASTADORES
-- ============================================================
INSERT INTO subastadores (identificador, matricula) VALUES
(20, 'MAT-ARG-2024-001'),
(21, 'MAT-ARG-2024-002');

-- ============================================================
-- DUEÑOS
-- ============================================================
INSERT INTO duenios (identificador, razon_social) VALUES
(30, 'Galería de Arte Borges S.R.L.'),
(31, 'Casa Subastas del Sur S.A.'),
(32, 'Smith Private Collection LLC');

-- ============================================================
-- USUARIOS AUTH (contraseña = "Password123!" — bcrypt)
-- ============================================================
-- Hash bcrypt de "Password123!" generado con strength 10
INSERT INTO usuarios_auth (id, persona, email, password_hash, estado, uuid) VALUES
(1, 1, 'juan.rodriguez@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHOC',
   'APROBADO','a1b2c3d4-e5f6-7890-abcd-ef1234567890'),
(2, 2, 'maria.gonzalez@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHOC',
   'APROBADO','b2c3d4e5-f6a7-890b-cdef-123456789012'),
(3, 3, 'carlos.martinez@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHOC',
   'APROBADO','c3d4e5f6-a7b8-90cd-ef01-234567890123'),
(4, 4, 'ana.lopez@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHOC',
   'APROBADO','d4e5f6a7-b8c9-0123-4567-890123456789'),
(5, 5, 'roberto.perez@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHOC',
   'APROBADO','e5f6a7b8-c9d0-1234-5678-901234567890'),
(6, 6, 'valentina.torres@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHOC',
   'PENDIENTE','f6a7b8c9-d0e1-2345-6789-012345678901'),
(7, 7, 'diego.sanchez@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHOC',
   'APROBADO','a7b8c9d0-e1f2-3456-789a-bc1234567890'),
(8, 8, 'laura.moreno@email.com',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHOC',
   'APROBADO','b8c9d0e1-f2a3-4567-89bc-d01234567890');

-- ============================================================
-- SEGUROS
-- ============================================================
INSERT INTO seguros (numero_poliza, compania, monto_cubierto, vigente, producto) VALUES
('SEG-2024-001', 'La Buenos Aires Seguros', 50000.00,   1, NULL),
('SEG-2024-002', 'Zurich Argentina',        120000.00,  1, NULL),
('SEG-2024-003', 'Sancor Seguros',           30000.00,  1, NULL),
('SEG-2024-004', 'Allianz Argentina',       200000.00,  1, NULL),
('SEG-2024-005', 'Mapfre Argentina',         15000.00,  1, NULL),
('SEG-2024-OBS', 'SMG Seguros',              8000.00,   0, NULL); -- vencido

-- ============================================================
-- PRODUCTOS
-- ============================================================
INSERT INTO productos (identificador, fecha, disponible, descripcionCatalogo, descripcionCompleta,
                        revisor, duenio, seguro, tipo_producto, declaracion_propiedad,
                        estado_revision, ubicacion_deposito) VALUES
(1, '2024-01-15', 'si',
   'Reloj Rolex Submariner - Acero inoxidable',
   'Rolex Submariner Date Ref. 16610, caja 40mm, bisel negro rotante, pulsera Oyster. Año 1998. Funciona perfectamente. Con caja original y papeles.',
   10, 30, 'SEG-2024-001', 'ESTANDAR', 1, 'ACEPTADO', 'Depósito A - Estante 3'),

(2, '2024-01-20', 'si',
   'Pintura al óleo "Atardecer en el Riachuelo" — Autor desconocido, s. XIX',
   'Óleo sobre tela. 80x60cm. Marco original de época. Excelente estado de conservación. Provenance documentada desde 1920.',
   10, 30, 'SEG-2024-002', 'ESTANDAR', 1, 'ACEPTADO', 'Depósito B - Sección Arte'),

(3, '2024-02-05', 'si',
   'Colección de 50 monedas antiguas argentinas (1880-1950)',
   'Lote de 50 monedas argentinas de plata y níquel. Incluye: 1 peso ley 1881, 50 centavos 1883, fichas de estancia. Estado general MBC-EBC.',
   11, 31, 'SEG-2024-003', 'ESTANDAR', 1, 'ACEPTADO', 'Depósito A - Caja Fuerte 1'),

(4, '2024-02-10', 'si',
   'Piano de Cola Steinway & Sons Modelo B',
   'Piano de cola Steinway & Sons Modelo B (211cm). Fabricado en Hamburgo, 1972. Lacado negro brillante. Recientemente afinado y revisado por técnico certificado.',
   10, 31, 'SEG-2024-004', 'ESTANDAR', 1, 'ACEPTADO', 'Depósito C - Sala Grande'),

(5, '2024-02-15', 'si',
   'Biblioteca de caoba victoriana — 12 cuerpos',
   'Biblioteca en caoba maciza estilo victoriano. 3.50m de alto, 6m de ancho (desmontable). Tallado artesanal. Puertas con vidrio emplomado. Circa 1890.',
   11, 32, 'SEG-2024-005', 'ESTANDAR', 1, 'ACEPTADO', 'Depósito C - Almacén 2'),

(6, '2024-03-01', 'si',
   'Figura de bronce "El Pensador" — Edición limitada',
   'Réplica autorizada de "El Pensador" de Rodin. Bronce fundido. Altura 45cm. Certificado de autenticidad. Edición limitada 12/100. Francia, 2005.',
   10, 30, 'SEG-2024-001', 'ESTANDAR', 1, 'ACEPTADO', 'Depósito B - Vitrina 5'),

-- Producto PENDIENTE de revisión
(7, '2024-03-10', 'no',
   NULL,
   'Máquina de escribir Olivetti Lettera 32. Color verde oliva. Funda original. Perfecto estado funcional.',
   NULL, 31, NULL, 'ESTANDAR', 0, 'PENDIENTE', NULL),

-- Producto RECHAZADO
(8, '2024-03-15', 'no',
   NULL,
   'Cuadro de autor desconocido sin documentación. Sin comprobante de propiedad.',
   10, 32, NULL, 'ESTANDAR', 0, 'RECHAZADO', NULL);

-- Asignar seguros a productos
UPDATE seguros SET producto = 1 WHERE numero_poliza = 'SEG-2024-001';
UPDATE seguros SET producto = 2 WHERE numero_poliza = 'SEG-2024-002';
UPDATE seguros SET producto = 3 WHERE numero_poliza = 'SEG-2024-003';
UPDATE seguros SET producto = 4 WHERE numero_poliza = 'SEG-2024-004';
UPDATE seguros SET producto = 5 WHERE numero_poliza = 'SEG-2024-005';

-- ============================================================
-- SUBASTAS
-- ============================================================
INSERT INTO subastas (identificador, fecha, hora, estado, categoria, ubicacion, moneda,
                       descripcion, subastador, capacidad_asistentes, tiene_deposito, seguridad_propia) VALUES
-- Subasta ABIERTA (en curso)
(1, CURDATE(), '19:00:00', 'abierta', 'comun',
   'Av. Corrientes 1346 Piso 3, CABA',
   'ARS',
   'Subasta de bienes preciados — Lote de marzo 2024. Relojes, arte y antigüedades.',
   20, 50, 'no', 'no'),

-- Subasta PROGRAMADA (futura)
(2, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '18:00:00', 'programada', 'oro',
   'Palacio Paz — Florida 1000, CABA',
   'ARS',
   'Subasta Premium — Arte y obras históricas. Solo para clientes categoría Oro y superior.',
   21, 30, 'si', 'si'),

-- Subasta CERRADA (histórica)
(3, DATE_SUB(CURDATE(), INTERVAL 15 DAY), '20:00:00', 'cerrada', 'comun',
   'Av. de Mayo 1370, CABA',
   'ARS',
   'Subasta de mobiliario y objetos de época — Lote enero 2024.',
   20, 40, 'no', 'no');

-- ============================================================
-- CATÁLOGOS
-- ============================================================
INSERT INTO catalogos (identificador, subasta) VALUES
(1, 1), -- Catálogo de la subasta abierta
(2, 2), -- Catálogo de la subasta futura
(3, 3); -- Catálogo de la subasta cerrada

-- ============================================================
-- ITEMS EN CATÁLOGOS
-- ============================================================
INSERT INTO itemsCatalogo (identificador, catalogo, producto, precioBase, comision, subastado, orden) VALUES
-- Subasta 1 (abierta) — items disponibles
(1, 1, 1, 25000.00, 5.00, 'no', 1),  -- Rolex
(2, 1, 6, 12000.00, 5.00, 'no', 2),  -- El Pensador bronce
(3, 1, 3,  8000.00, 3.00, 'no', 3),  -- Monedas
-- Subasta 2 (futura)
(4, 2, 2, 80000.00, 8.00, 'no', 1),  -- Pintura
(5, 2, 4, 95000.00, 8.00, 'no', 2),  -- Piano Steinway
-- Subasta 3 (cerrada — items ya subastados)
(6, 3, 5, 45000.00, 5.00, 'si', 1);  -- Biblioteca

-- ============================================================
-- ASISTENTES A SUBASTAS
-- ============================================================
INSERT INTO asistentes (identificador, cliente, subasta, numero_postor) VALUES
(1, 1, 1, 101), -- Juan en subasta 1
(2, 2, 1, 102), -- María en subasta 1
(3, 3, 1, 103), -- Carlos en subasta 1
(4, 5, 1, 104), -- Roberto en subasta 1
(5, 7, 1, 105), -- Diego en subasta 1
(6, 7, 3, 201), -- Diego en subasta cerrada
(7, 8, 3, 202); -- Laura en subasta cerrada

-- ============================================================
-- MEDIOS DE PAGO
-- ============================================================
INSERT INTO mediosPago (identificador, cliente, tipo, banco, numero_cuenta, tipo_cuenta,
                         es_internacional, verificado, activo, monto_reservado) VALUES
(1, 1, 'CUENTA_BANCARIA', 'Banco Galicia', '0000123456789', 'CORRIENTE', 0, 1, 1, 0.00),
(2, 2, 'CUENTA_BANCARIA', 'Banco Santander', '0000987654321', 'CAJA_DE_AHORRO', 0, 1, 1, 0.00),
(3, 3, 'CUENTA_BANCARIA', 'HSBC Argentina', '0000555666777', 'CORRIENTE', 1, 1, 1, 0.00),
(4, 5, 'CUENTA_BANCARIA', 'Banco Macro', '0000111222333', 'CORRIENTE', 0, 1, 1, 0.00),
(5, 7, 'CUENTA_BANCARIA', 'Banco Nación', '0000444555666', 'CORRIENTE', 0, 1, 1, 24000.00),
(6, 8, 'CUENTA_BANCARIA', 'BBVA Argentina', '0000777888999', 'CORRIENTE', 1, 1, 1, 0.00);

-- Tarjetas (adicionales)
INSERT INTO mediosPago (identificador, cliente, tipo, numero_tarjeta, titular_tarjeta,
                         vencimiento, marca, es_internacional, verificado, activo, monto_reservado) VALUES
(7, 2, 'TARJETA_CREDITO', '4111111111111111', 'MARIA LAURA GONZALEZ',
   '2027-12-31', 'VISA', 0, 1, 1, 0.00),
(8, 8, 'TARJETA_CREDITO', '5500005555555559', 'LAURA SILVINA MORENO',
   '2028-06-30', 'MASTERCARD', 1, 1, 1, 0.00);

-- ============================================================
-- PUJAS HISTÓRICAS (subasta 3 cerrada)
-- ============================================================
INSERT INTO pujos (identificador, asistente, item, importe, ganador, fecha_hora) VALUES
(1, 6, 6, 45000.00, 'no', DATE_SUB(NOW(), INTERVAL 15 DAY)),  -- Diego primera puja
(2, 7, 6, 48000.00, 'no', DATE_SUB(NOW(), INTERVAL 15 DAY)),  -- Laura supera
(3, 6, 6, 52000.00, 'no', DATE_SUB(NOW(), INTERVAL 15 DAY)),  -- Diego vuelve
(4, 7, 6, 58000.00, 'si', DATE_SUB(NOW(), INTERVAL 15 DAY));  -- Laura gana

-- ============================================================
-- REGISTRO DE SUBASTA (venta realizada en subasta 3)
-- ============================================================
INSERT INTO registrosSubasta (identificador, subasta, duenio, producto, cliente, importe, comision, fecha_registro) VALUES
(1, 3, 32, 5, 8, 58000.00, 2900.00, DATE_SUB(NOW(), INTERVAL 15 DAY));

-- ============================================================
-- PUJAS EN CURSO (subasta 1 abierta — Rolex en disputa)
-- ============================================================
INSERT INTO pujos (identificador, asistente, item, importe, ganador, fecha_hora) VALUES
(5, 1, 1, 25000.00, 'no', DATE_SUB(NOW(), INTERVAL 30 MINUTE)), -- Juan abre
(6, 2, 1, 27500.00, 'no', DATE_SUB(NOW(), INTERVAL 25 MINUTE)), -- María supera
(7, 3, 1, 30000.00, 'no', DATE_SUB(NOW(), INTERVAL 20 MINUTE)), -- Carlos
(8, 5, 1, 33000.00, 'no', DATE_SUB(NOW(), INTERVAL 15 MINUTE)), -- Roberto
(9, 1, 1, 36000.00, 'si', DATE_SUB(NOW(), INTERVAL 5 MINUTE));  -- Juan va ganando

-- Actualizar reserva de fondos (Juan tiene 36000 reservados)
UPDATE mediosPago SET monto_reservado = 36000.00 WHERE identificador = 1;

-- ============================================================
-- NOTIFICACIONES DE PRUEBA
-- ============================================================
INSERT INTO notificaciones (id, cliente, tipo, titulo, mensaje, leida, referencia_id, referencia_tipo, fecha_creacion) VALUES
(1, 1, 'PUJA_GANADA', '¡Estás ganando!',
 'Sos el mejor postor por el Rolex Submariner con $36.000. La subasta sigue abierta.',
 0, 1, 'ITEM', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),

(2, 2, 'PUJA_SUPERADA', 'Tu puja fue superada',
 'Carlos Martínez superó tu oferta en el Rolex Submariner. Nueva oferta: $30.000.',
 0, 1, 'ITEM', DATE_SUB(NOW(), INTERVAL 20 MINUTE)),

(3, 8, 'PUJA_GANADA', '¡Ganaste la subasta!',
 'Felicitaciones, ganaste la Biblioteca Victoriana por $58.000 en la subasta del 16/03.',
 1, 6, 'ITEM', DATE_SUB(NOW(), INTERVAL 15 DAY)),

(4, 7, 'USUARIO_APROBADO', 'Cuenta aprobada',
 'Tu cuenta fue aprobada. Ya podés participar en subastas.',
 1, NULL, NULL, DATE_SUB(NOW(), INTERVAL 30 DAY));

-- ============================================================
-- VERIFICACIÓN FINAL
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅ Base de datos cargada exitosamente!' AS resultado;
SELECT TABLE_NAME, TABLE_ROWS
FROM information_schema.tables
WHERE table_schema = 'subastas_bd'
ORDER BY TABLE_NAME;
