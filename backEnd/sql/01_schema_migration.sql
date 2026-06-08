-- ============================================================
-- SUBASTAS G16 — Script completo MySQL
-- Crea la DB, tablas (migración de SQL Server) y datos de prueba
-- Ejecutar contra: localhost:3306 | root / pochito
-- ============================================================

-- ---- 0. Crear y usar la base (limpia) ----
DROP DATABASE IF EXISTS subastas_bd;

CREATE DATABASE subastas_bd
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE subastas_bd;

-- Desactivar FK checks durante la carga
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- TABLAS DE REFERENCIA / CATÁLOGOS
-- ============================================================

CREATE TABLE IF NOT EXISTS paises (
    identificador INT AUTO_INCREMENT PRIMARY KEY,
    descripcion   VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- PERSONAS (base para clientes, empleados, subastadores, dueños)
-- ============================================================

CREATE TABLE IF NOT EXISTS personas (
    identificador INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(200) NOT NULL,
    documento     VARCHAR(50)  NOT NULL UNIQUE,
    direccion     VARCHAR(300),
    pais          INT,
    estado        VARCHAR(15)  DEFAULT 'activo',
    foto          LONGBLOB,
    CONSTRAINT fk_personas_pais FOREIGN KEY (pais) REFERENCES paises(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- USUARIOS DE AUTENTICACIÓN (nueva tabla, no en legacy)
-- ============================================================

CREATE TABLE IF NOT EXISTS usuarios_auth (
<<<<<<< HEAD
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id    INT         NOT NULL UNIQUE,
    email         VARCHAR(200) NOT NULL UNIQUE,
    password_hash VARCHAR(300) NOT NULL,
    estado        VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    uuid          VARCHAR(36)  NOT NULL UNIQUE,
    foto_doc_frente LONGBLOB,
    foto_doc_dorso  LONGBLOB,
    fecha_registro DATETIME    DEFAULT CURRENT_TIMESTAMP,
=======
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id      INT          NOT NULL UNIQUE,
    email           VARCHAR(200) NOT NULL UNIQUE,
    password_hash   VARCHAR(300) NOT NULL,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    uuid            VARCHAR(36)  NOT NULL UNIQUE,
    foto_doc_frente LONGBLOB,
    foto_doc_dorso  LONGBLOB,
    fecha_registro  DATETIME     DEFAULT CURRENT_TIMESTAMP,
>>>>>>> ca6dc94214080f53249763627adfc6a129c21c2d
    CONSTRAINT fk_ua_persona FOREIGN KEY (persona_id) REFERENCES personas(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- CLIENTES
-- ============================================================

CREATE TABLE IF NOT EXISTS clientes (
    identificador INT PRIMARY KEY,
    pais          INT,
    categoria     VARCHAR(20) NOT NULL DEFAULT 'comun',
    admitido      VARCHAR(2)  NOT NULL DEFAULT 'no',
    verificador   INT NOT NULL,
    CONSTRAINT fk_cliente_persona FOREIGN KEY (identificador) REFERENCES personas(identificador),
    CONSTRAINT fk_cliente_pais    FOREIGN KEY (pais)          REFERENCES paises(identificador),
    CONSTRAINT fk_cliente_verificador FOREIGN KEY (verificador) REFERENCES empleados(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- EMPLEADOS
-- ============================================================

CREATE TABLE IF NOT EXISTS empleados (
    identificador   INT PRIMARY KEY,
    legajo          VARCHAR(50),
    sector          VARCHAR(100),
    CONSTRAINT fk_empleado_persona FOREIGN KEY (identificador) REFERENCES personas(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- SUBASTADORES
-- ============================================================

CREATE TABLE IF NOT EXISTS subastadores (
    identificador   INT PRIMARY KEY,
    matricula       VARCHAR(50),
    CONSTRAINT fk_subastador_persona FOREIGN KEY (identificador) REFERENCES personas(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- DUEÑOS DE BIENES
-- ============================================================

CREATE TABLE IF NOT EXISTS duenios (
    identificador          INT PRIMARY KEY,
    razon_social           VARCHAR(200),
    numeroPais             INT,
    verificacionFinanciera VARCHAR(2) DEFAULT 'si',
    verificacionJudicial   VARCHAR(2) DEFAULT 'si',
    calificacionRiesgo     INT DEFAULT 1,
    verificador            INT NOT NULL,
    CONSTRAINT fk_duenio_persona FOREIGN KEY (identificador) REFERENCES personas(identificador),
    CONSTRAINT fk_duenio_pais FOREIGN KEY (numeroPais) REFERENCES paises(identificador),
    CONSTRAINT fk_duenio_verificador FOREIGN KEY (verificador) REFERENCES empleados(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- SEGUROS (vinculados a productos)
-- ============================================================

CREATE TABLE IF NOT EXISTS seguros (
    numero_poliza   VARCHAR(50) PRIMARY KEY,
    compania        VARCHAR(200),
    monto_cubierto  DECIMAL(18,2),
    vigente         TINYINT(1) NOT NULL DEFAULT 1,
    producto        INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- PRODUCTOS
-- ============================================================

CREATE TABLE IF NOT EXISTS productos (
    identificador           INT AUTO_INCREMENT PRIMARY KEY,
    fecha                   DATE,
    disponible              VARCHAR(2)  DEFAULT 'si',
    descripcionCatalogo     VARCHAR(500),
    descripcionCompleta     VARCHAR(300) NOT NULL,
    revisor                 INT,
    duenio                  INT NOT NULL,
    seguro                  VARCHAR(50),
    tipo_producto           VARCHAR(20) DEFAULT 'ESTANDAR',
    declaracion_propiedad   TINYINT(1)  DEFAULT 0,
    estado_revision         VARCHAR(20) DEFAULT 'PENDIENTE',
    motivo_rechazo          VARCHAR(500),
    ubicacion_deposito      VARCHAR(200),
    CONSTRAINT fk_prod_revisor FOREIGN KEY (revisor) REFERENCES empleados(identificador),
    CONSTRAINT fk_prod_duenio  FOREIGN KEY (duenio)  REFERENCES duenios(identificador),
    CONSTRAINT fk_prod_seguro  FOREIGN KEY (seguro)  REFERENCES seguros(numero_poliza)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Agregar FK de seguros → productos (circular, se agrega después)
ALTER TABLE seguros
    ADD CONSTRAINT fk_seg_producto FOREIGN KEY (producto) REFERENCES productos(identificador);

-- ============================================================
-- FOTOS DE PRODUCTOS
-- ============================================================

CREATE TABLE IF NOT EXISTS fotos (
    identificador INT AUTO_INCREMENT PRIMARY KEY,
    producto      INT NOT NULL,
    foto          LONGBLOB NOT NULL,
    CONSTRAINT fk_foto_producto FOREIGN KEY (producto) REFERENCES productos(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- PRODUCTOS OBRA DE ARTE (herencia)
-- ============================================================

CREATE TABLE IF NOT EXISTS productos_obra_arte (
    producto_id     INT PRIMARY KEY,
    artista         VARCHAR(200),
    disenador       VARCHAR(200),
    fecha_creacion  DATE,
    historia        TEXT,
    CONSTRAINT fk_obra_producto FOREIGN KEY (producto_id) REFERENCES productos(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- SUBASTAS
-- ============================================================

CREATE TABLE IF NOT EXISTS subastas (
    identificador           INT AUTO_INCREMENT PRIMARY KEY,
    fecha                   DATE,
    hora                    TIME,
    estado                  VARCHAR(20)  DEFAULT 'abierta',
    categoria               VARCHAR(20),
    ubicacion               VARCHAR(300),
    moneda                  VARCHAR(3)   DEFAULT 'ARS',
    descripcion             VARCHAR(500),
    subastador              INT,
    capacidad_asistentes    INT,
    tiene_deposito          VARCHAR(2)   DEFAULT 'no',
    seguridad_propia        VARCHAR(2)   DEFAULT 'no',
    CONSTRAINT fk_sub_subastador FOREIGN KEY (subastador) REFERENCES subastadores(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- CATÁLOGOS
-- ============================================================

CREATE TABLE IF NOT EXISTS catalogos (
    identificador   INT AUTO_INCREMENT PRIMARY KEY,
    subasta         INT NOT NULL UNIQUE,
    responsable     INT NOT NULL,
    CONSTRAINT fk_cat_subasta FOREIGN KEY (subasta) REFERENCES subastas(identificador),
    CONSTRAINT fk_cat_responsable FOREIGN KEY (responsable) REFERENCES empleados(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- ITEMS DEL CATÁLOGO
-- ============================================================

CREATE TABLE IF NOT EXISTS itemsCatalogo (
    identificador   INT AUTO_INCREMENT PRIMARY KEY,
    catalogo        INT NOT NULL,
    producto        INT NOT NULL,
    precioBase      DECIMAL(18,2) NOT NULL,
    comision        DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    subastado       VARCHAR(2)   DEFAULT 'no',
    orden           INT,
    CONSTRAINT fk_item_catalogo FOREIGN KEY (catalogo) REFERENCES catalogos(identificador),
    CONSTRAINT fk_item_producto FOREIGN KEY (producto) REFERENCES productos(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- ASISTENTES A SUBASTAS
-- ============================================================

CREATE TABLE IF NOT EXISTS asistentes (
    identificador   INT AUTO_INCREMENT PRIMARY KEY,
    cliente         INT NOT NULL,
    subasta         INT NOT NULL,
    numero_postor   INT NOT NULL,
    UNIQUE KEY uq_asistente (cliente, subasta),
    CONSTRAINT fk_asis_cliente FOREIGN KEY (cliente) REFERENCES clientes(identificador),
    CONSTRAINT fk_asis_subasta FOREIGN KEY (subasta) REFERENCES subastas(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- SESIONES ACTIVAS EN SUBASTAS (nueva tabla)
-- ============================================================

CREATE TABLE IF NOT EXISTS sesiones_subasta (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id      INT NOT NULL UNIQUE,
    subasta_id      INT NOT NULL,
    fecha_conexion  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ses_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(identificador),
    CONSTRAINT fk_ses_subasta FOREIGN KEY (subasta_id) REFERENCES subastas(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- PUJOS (PUJAS)
-- ============================================================

CREATE TABLE IF NOT EXISTS pujos (
    identificador   INT AUTO_INCREMENT PRIMARY KEY,
    asistente       INT NOT NULL,
    item            INT NOT NULL,
    importe         DECIMAL(18,2) NOT NULL,
    ganador         VARCHAR(2)   DEFAULT 'si',
    fecha_hora      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pujo_asistente FOREIGN KEY (asistente) REFERENCES asistentes(identificador),
    CONSTRAINT fk_pujo_item      FOREIGN KEY (item)      REFERENCES itemsCatalogo(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MEDIOS DE PAGO
-- ============================================================

CREATE TABLE IF NOT EXISTS medios_pago (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id          INT NOT NULL,
    tipo                VARCHAR(30) NOT NULL,
    banco               VARCHAR(150),
    numero_cuenta       VARCHAR(50),
    cbu_swift           VARCHAR(50),
    es_internacional    BOOLEAN DEFAULT FALSE,
    numero_tarjeta_hash VARCHAR(255),
    titular             VARCHAR(150),
    vencimiento         VARCHAR(7),
    es_tarjeta_internacional BOOLEAN DEFAULT FALSE,
    numero_cheque       VARCHAR(50),
    banco_emisor        VARCHAR(150),
    monto_certificado   DECIMAL(18,2),
    moneda              VARCHAR(3) NOT NULL,
    verificado          BOOLEAN DEFAULT FALSE,
    activo              BOOLEAN DEFAULT TRUE,
    fecha_registro      DATETIME DEFAULT CURRENT_TIMESTAMP,
    monto_reservado     DECIMAL(18,2) DEFAULT 0.00,
    CONSTRAINT fk_mp_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- REGISTROS DE SUBASTA (ventas realizadas)
-- ============================================================

CREATE TABLE IF NOT EXISTS registrosSubasta (
    identificador   INT AUTO_INCREMENT PRIMARY KEY,
    subasta         INT NOT NULL,
    duenio          INT NOT NULL,
    producto        INT NOT NULL,
    cliente         INT NOT NULL,
    importe         DECIMAL(18,2) NOT NULL,
    comision        DECIMAL(18,2) DEFAULT 0.00,
    fecha_registro  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_subasta  FOREIGN KEY (subasta)  REFERENCES subastas(identificador),
    CONSTRAINT fk_reg_duenio   FOREIGN KEY (duenio)   REFERENCES duenios(identificador),
    CONSTRAINT fk_reg_producto FOREIGN KEY (producto) REFERENCES productos(identificador),
    CONSTRAINT fk_reg_cliente  FOREIGN KEY (cliente)  REFERENCES clientes(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MULTAS
-- ============================================================

CREATE TABLE IF NOT EXISTS multas (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente             INT NOT NULL,
    subasta             INT,
    item                INT,
    monto_ofertado      DECIMAL(18,2) NOT NULL,
    monto_multa         DECIMAL(18,2) NOT NULL,
    pagada              TINYINT(1)   NOT NULL DEFAULT 0,
    fecha_limite        DATETIME,
    derivado_justicia   TINYINT(1)   NOT NULL DEFAULT 0,
    fecha_creacion      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_multa_cliente FOREIGN KEY (cliente) REFERENCES clientes(identificador),
    CONSTRAINT fk_multa_subasta FOREIGN KEY (subasta) REFERENCES subastas(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- NOTIFICACIONES
-- ============================================================

CREATE TABLE IF NOT EXISTS notificaciones (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id      INT NOT NULL,
    tipo            VARCHAR(50)  NOT NULL,
    titulo          VARCHAR(200) NOT NULL,
    mensaje         TEXT,
    leida           TINYINT(1)   NOT NULL DEFAULT 0,
    referencia_id   BIGINT,
    referencia_tipo VARCHAR(30),
    fecha_creacion  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(identificador)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- REACTIVAR FK checks
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;
