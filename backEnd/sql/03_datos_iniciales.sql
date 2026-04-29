-- ============================================================
-- DATOS INICIALES: Países + Empleado sistema
-- Ejecutar DESPUÉS de 01 y 02
-- ============================================================

USE subastas;

-- ============================================================
-- Empleado del sistema (necesario como verificador por defecto)
-- ============================================================
INSERT INTO personas (documento, nombre, direccion, estado) VALUES
('SISTEMA', 'Empleado Sistema', 'Sistema interno', 'activo');

SET @sistema_id = LAST_INSERT_ID();

INSERT INTO empleados (identificador, cargo, sector) VALUES
(@sistema_id, 'Administrador del Sistema', NULL);

-- ============================================================
-- Países (lista hardcodeada)
-- ============================================================
INSERT INTO paises (descripcion, gentilicio, idiomas) VALUES
('Argentina', 'Argentino/a', 'Español'),
('Brasil', 'Brasileño/a', 'Portugués'),
('Chile', 'Chileno/a', 'Español'),
('Uruguay', 'Uruguayo/a', 'Español'),
('Paraguay', 'Paraguayo/a', 'Español, Guaraní'),
('Bolivia', 'Boliviano/a', 'Español'),
('Perú', 'Peruano/a', 'Español'),
('Colombia', 'Colombiano/a', 'Español'),
('Ecuador', 'Ecuatoriano/a', 'Español'),
('Venezuela', 'Venezolano/a', 'Español'),
('México', 'Mexicano/a', 'Español'),
('Estados Unidos', 'Estadounidense', 'Inglés'),
('Canadá', 'Canadiense', 'Inglés, Francés'),
('España', 'Español/a', 'Español'),
('Francia', 'Francés/a', 'Francés'),
('Italia', 'Italiano/a', 'Italiano'),
('Alemania', 'Alemán/a', 'Alemán'),
('Reino Unido', 'Británico/a', 'Inglés'),
('Japón', 'Japonés/a', 'Japonés'),
('China', 'Chino/a', 'Mandarín'),
('Australia', 'Australiano/a', 'Inglés'),
('Suiza', 'Suizo/a', 'Alemán, Francés, Italiano'),
('Países Bajos', 'Neerlandés/a', 'Neerlandés'),
('Portugal', 'Portugués/a', 'Portugués'),
('Corea del Sur', 'Surcoreano/a', 'Coreano');
