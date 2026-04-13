-- ============================================================
-- TABLAS NUEVAS: Extensiones para el sistema de subastas
-- Ejecutar DESPUÉS de 01_estructura_legacy_mysql.sql
-- ============================================================

USE subastas;

-- ============================================================
-- TABLA: usuarios_auth (credenciales de login)
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios_auth (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    estado ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'BLOQUEADO') DEFAULT 'PENDIENTE',
    foto_doc_frente LONGBLOB,
    foto_doc_dorso LONGBLOB,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_persona FOREIGN KEY (persona_id) REFERENCES personas(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: medios_pago
-- ============================================================
CREATE TABLE IF NOT EXISTS medios_pago (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    tipo ENUM('CUENTA_BANCARIA', 'TARJETA_CREDITO', 'CHEQUE_CERTIFICADO') NOT NULL,
    banco VARCHAR(150),
    numero_cuenta VARCHAR(50),
    cbu_swift VARCHAR(50),
    es_internacional BOOLEAN DEFAULT FALSE,
    numero_tarjeta_hash VARCHAR(255),
    titular VARCHAR(150),
    vencimiento VARCHAR(7),
    es_tarjeta_internacional BOOLEAN DEFAULT FALSE,
    numero_cheque VARCHAR(50),
    banco_emisor VARCHAR(150),
    monto_certificado DECIMAL(18,2),
    moneda ENUM('ARS', 'USD') NOT NULL,
    verificado BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    monto_reservado DECIMAL(18,2) DEFAULT 0.00,
    CONSTRAINT fk_mp_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: notificaciones
-- ============================================================
CREATE TABLE IF NOT EXISTS notificaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    tipo ENUM('PUJA_GANADA', 'PUJA_SUPERADA', 'USUARIO_APROBADO', 'USUARIO_RECHAZADO',
              'PRODUCTO_ACEPTADO', 'PRODUCTO_RECHAZADO', 'SUBASTA_PROXIMA', 'PAGO_PENDIENTE',
              'MULTA', 'GENERAL') NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    referencia_id BIGINT,
    referencia_tipo VARCHAR(50),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: sesiones_subasta (1 subasta activa por usuario)
-- ============================================================
CREATE TABLE IF NOT EXISTS sesiones_subasta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL UNIQUE,
    subasta_id INT NOT NULL,
    fecha_conexion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sesion_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(identificador),
    CONSTRAINT fk_sesion_subasta FOREIGN KEY (subasta_id) REFERENCES subastas(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: multas
-- ============================================================
CREATE TABLE IF NOT EXISTS multas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    subasta_id INT NOT NULL,
    item_id INT NOT NULL,
    monto_ofertado DECIMAL(18,2) NOT NULL,
    monto_multa DECIMAL(18,2) NOT NULL,
    pagada BOOLEAN DEFAULT FALSE,
    fecha_limite DATETIME NOT NULL,
    derivado_justicia BOOLEAN DEFAULT FALSE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_multa_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(identificador),
    CONSTRAINT fk_multa_subasta FOREIGN KEY (subasta_id) REFERENCES subastas(identificador),
    CONSTRAINT fk_multa_item FOREIGN KEY (item_id) REFERENCES itemsCatalogo(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: productos_obra_arte (extensión para obras de arte)
-- ============================================================
CREATE TABLE IF NOT EXISTS productos_obra_arte (
    producto_id INT NOT NULL,
    artista VARCHAR(200),
    disenador VARCHAR(200),
    fecha_creacion DATE,
    historia TEXT,
    CONSTRAINT pk_prod_arte PRIMARY KEY (producto_id),
    CONSTRAINT fk_prod_arte FOREIGN KEY (producto_id) REFERENCES productos(identificador)
) ENGINE=InnoDB;
