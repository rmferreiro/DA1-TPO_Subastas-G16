-- ============================================================
-- MIGRACIÓN: Estructura Legacy SQL Server → MySQL
-- Sistema de Subastas - UADE DA1 Grupo 16
-- Ejecutar en orden sobre la base de datos 'subastas'
-- ============================================================

-- Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS subastas
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE subastas;

-- ============================================================
-- TABLA: paises (catálogo de países)
-- ============================================================
CREATE TABLE IF NOT EXISTS paises (
    numero INT NOT NULL AUTO_INCREMENT,
    descripcion VARCHAR(100) NOT NULL,
    gentilicio VARCHAR(100),
    idiomas VARCHAR(150) NOT NULL,
    CONSTRAINT pk_paises PRIMARY KEY (numero)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: personas (tabla base para clientes, dueños, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS personas (
    identificador INT NOT NULL AUTO_INCREMENT,
    documento VARCHAR(20) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    direccion VARCHAR(250),
    estado VARCHAR(15) DEFAULT 'activo',
    foto LONGBLOB,
    CONSTRAINT pk_personas PRIMARY KEY (identificador),
    CONSTRAINT chk_estado CHECK (estado IN ('activo', 'inactivo'))
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: empleados
-- ============================================================
CREATE TABLE IF NOT EXISTS empleados (
    identificador INT NOT NULL,
    cargo VARCHAR(100),
    sector INT NULL,
    CONSTRAINT pk_empleados PRIMARY KEY (identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: sectores
-- ============================================================
CREATE TABLE IF NOT EXISTS sectores (
    identificador INT NOT NULL AUTO_INCREMENT,
    nombreSector VARCHAR(150) NOT NULL,
    codigoSector VARCHAR(10) NULL,
    responsableSector INT NULL,
    CONSTRAINT pk_sectores PRIMARY KEY (identificador),
    CONSTRAINT fk_sectores_empleados FOREIGN KEY (responsableSector) REFERENCES empleados(identificador)
) ENGINE=InnoDB;

-- Agregar FK de empleados a sectores (referencia circular, se agrega después)
ALTER TABLE empleados
    ADD CONSTRAINT fk_empleados_personas FOREIGN KEY (identificador) REFERENCES personas(identificador),
    ADD CONSTRAINT fk_empleados_sectores FOREIGN KEY (sector) REFERENCES sectores(identificador);

-- ============================================================
-- TABLA: seguros
-- ============================================================
CREATE TABLE IF NOT EXISTS seguros (
    nroPoliza VARCHAR(30) NOT NULL,
    compania VARCHAR(150) NOT NULL,
    polizaCombinada VARCHAR(2),
    importe DECIMAL(18,2) NOT NULL,
    CONSTRAINT pk_seguros PRIMARY KEY (nroPoliza),
    CONSTRAINT chk_polizaCombinada CHECK (polizaCombinada IN ('si','no')),
    CONSTRAINT chk_importe CHECK (importe > 0)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: clientes (postores/compradores)
-- ============================================================
CREATE TABLE IF NOT EXISTS clientes (
    identificador INT NOT NULL,
    numeroPais INT,
    admitido VARCHAR(2) DEFAULT 'no',
    categoria VARCHAR(10),
    verificador INT NOT NULL,
    CONSTRAINT pk_clientes PRIMARY KEY (identificador),
    CONSTRAINT chk_admitido CHECK (admitido IN ('si','no')),
    CONSTRAINT chk_categoria CHECK (categoria IN ('comun', 'especial', 'plata', 'oro', 'platino')),
    CONSTRAINT fk_clientes_personas FOREIGN KEY (identificador) REFERENCES personas(identificador),
    CONSTRAINT fk_clientes_empleados FOREIGN KEY (verificador) REFERENCES empleados(identificador),
    CONSTRAINT fk_clientes_paises FOREIGN KEY (numeroPais) REFERENCES paises(numero)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: duenios (dueños de artículos)
-- ============================================================
CREATE TABLE IF NOT EXISTS duenios (
    identificador INT NOT NULL,
    numeroPais INT,
    verificacionFinanciera VARCHAR(2),
    verificacionJudicial VARCHAR(2),
    calificacionRiesgo INT,
    verificador INT NOT NULL,
    CONSTRAINT pk_duenios PRIMARY KEY (identificador),
    CONSTRAINT chk_VF CHECK (verificacionFinanciera IN ('si','no')),
    CONSTRAINT chk_VJ CHECK (verificacionJudicial IN ('si','no')),
    CONSTRAINT chk_CR CHECK (calificacionRiesgo IN (1,2,3,4,5,6)),
    CONSTRAINT fk_duenios_personas FOREIGN KEY (identificador) REFERENCES personas(identificador),
    CONSTRAINT fk_duenios_empleados FOREIGN KEY (verificador) REFERENCES empleados(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: subastadores (rematadores/martilleros)
-- ============================================================
CREATE TABLE IF NOT EXISTS subastadores (
    identificador INT NOT NULL,
    matricula VARCHAR(15),
    region VARCHAR(50),
    CONSTRAINT pk_subastadores PRIMARY KEY (identificador),
    CONSTRAINT fk_subastadores_personas FOREIGN KEY (identificador) REFERENCES personas(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: subastas
-- ============================================================
CREATE TABLE IF NOT EXISTS subastas (
    identificador INT NOT NULL AUTO_INCREMENT,
    fecha DATE,
    hora TIME NOT NULL,
    estado VARCHAR(10) DEFAULT 'abierta',
    subastador INT NULL,
    ubicacion VARCHAR(350) NULL,
    capacidadAsistentes INT NULL,
    tieneDeposito VARCHAR(2),
    seguridadPropia VARCHAR(2),
    categoria VARCHAR(10),
    -- Campos nuevos (no existen en Legacy)
    moneda VARCHAR(3) DEFAULT 'ARS',
    descripcion VARCHAR(500),
    item_actual_id INT NULL,
    CONSTRAINT pk_subastas PRIMARY KEY (identificador),
    CONSTRAINT chk_ES CHECK (estado IN ('abierta','cerrada')),
    CONSTRAINT chk_TD CHECK (tieneDeposito IN ('si','no')),
    CONSTRAINT chk_SP CHECK (seguridadPropia IN ('si','no')),
    CONSTRAINT chk_CS CHECK (categoria IN ('comun', 'especial', 'plata', 'oro', 'platino')),
    CONSTRAINT fk_subastas_subastadores FOREIGN KEY (subastador) REFERENCES subastadores(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: productos
-- ============================================================
CREATE TABLE IF NOT EXISTS productos (
    identificador INT NOT NULL AUTO_INCREMENT,
    fecha DATE,
    disponible VARCHAR(2) DEFAULT 'no',
    descripcionCatalogo VARCHAR(500) DEFAULT 'No Posee',
    descripcionCompleta VARCHAR(300) NOT NULL,
    revisor INT NOT NULL,
    duenio INT NOT NULL,
    seguro VARCHAR(30) NULL,
    -- Campos nuevos
    tipo_producto VARCHAR(20) DEFAULT 'ESTANDAR',
    declaracion_propiedad BOOLEAN DEFAULT FALSE,
    estado_revision VARCHAR(20) DEFAULT 'PENDIENTE',
    motivo_rechazo VARCHAR(500),
    ubicacion_deposito VARCHAR(200),
    CONSTRAINT pk_productos PRIMARY KEY (identificador),
    CONSTRAINT chk_D CHECK (disponible IN ('si','no')),
    CONSTRAINT fk_productos_empleados FOREIGN KEY (revisor) REFERENCES empleados(identificador),
    CONSTRAINT fk_productos_duenios FOREIGN KEY (duenio) REFERENCES duenios(identificador),
    CONSTRAINT fk_productos_seguros FOREIGN KEY (seguro) REFERENCES seguros(nroPoliza)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: fotos
-- ============================================================
CREATE TABLE IF NOT EXISTS fotos (
    identificador INT NOT NULL AUTO_INCREMENT,
    producto INT NOT NULL,
    foto LONGBLOB NOT NULL,
    CONSTRAINT pk_fotos PRIMARY KEY (identificador),
    CONSTRAINT fk_fotos_productos FOREIGN KEY (producto) REFERENCES productos(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: catalogos
-- ============================================================
CREATE TABLE IF NOT EXISTS catalogos (
    identificador INT NOT NULL AUTO_INCREMENT,
    descripcion VARCHAR(250) NOT NULL,
    subasta INT NULL,
    responsable INT NOT NULL,
    CONSTRAINT pk_catalogos PRIMARY KEY (identificador),
    CONSTRAINT fk_catalogos_empleados FOREIGN KEY (responsable) REFERENCES empleados(identificador),
    CONSTRAINT fk_catalogos_subastas FOREIGN KEY (subasta) REFERENCES subastas(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: itemsCatalogo
-- ============================================================
CREATE TABLE IF NOT EXISTS itemsCatalogo (
    identificador INT NOT NULL AUTO_INCREMENT,
    catalogo INT NOT NULL,
    producto INT NOT NULL,
    precioBase DECIMAL(18,2) NOT NULL,
    comision DECIMAL(18,2) NOT NULL,
    subastado VARCHAR(2) DEFAULT 'no',
    CONSTRAINT pk_itemsCatalogo PRIMARY KEY (identificador),
    CONSTRAINT chk_PB CHECK (precioBase > 0.01),
    CONSTRAINT chk_C CHECK (comision > 0.01),
    CONSTRAINT chk_S CHECK (subastado IN ('si','no')),
    CONSTRAINT fk_itemsCatalogo_catalogos FOREIGN KEY (catalogo) REFERENCES catalogos(identificador),
    CONSTRAINT fk_itemsCatalogo_productos FOREIGN KEY (producto) REFERENCES productos(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: asistentes
-- ============================================================
CREATE TABLE IF NOT EXISTS asistentes (
    identificador INT NOT NULL AUTO_INCREMENT,
    numeroPostor INT NOT NULL,
    cliente INT NOT NULL,
    subasta INT NOT NULL,
    CONSTRAINT pk_asistentes PRIMARY KEY (identificador),
    CONSTRAINT fk_asistentes_clientes FOREIGN KEY (cliente) REFERENCES clientes(identificador),
    CONSTRAINT fk_asistentes_subasta FOREIGN KEY (subasta) REFERENCES subastas(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: pujos
-- ============================================================
CREATE TABLE IF NOT EXISTS pujos (
    identificador INT NOT NULL AUTO_INCREMENT,
    asistente INT NOT NULL,
    item INT NOT NULL,
    importe DECIMAL(18,2) NOT NULL,
    ganador VARCHAR(2) DEFAULT 'no',
    -- Campo nuevo
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_pujos PRIMARY KEY (identificador),
    CONSTRAINT chk_I CHECK (importe > 0.01),
    CONSTRAINT chk_G CHECK (ganador IN ('si','no')),
    CONSTRAINT fk_pujos_asistentes FOREIGN KEY (asistente) REFERENCES asistentes(identificador),
    CONSTRAINT fk_pujos_itemsCatalogo FOREIGN KEY (item) REFERENCES itemsCatalogo(identificador)
) ENGINE=InnoDB;

-- ============================================================
-- TABLA: registroDeSubasta
-- ============================================================
CREATE TABLE IF NOT EXISTS registroDeSubasta (
    identificador INT NOT NULL AUTO_INCREMENT,
    subasta INT NOT NULL,
    duenio INT NOT NULL,
    producto INT NOT NULL,
    cliente INT NOT NULL,
    importe DECIMAL(18,2) NOT NULL,
    comision DECIMAL(18,2) NOT NULL,
    CONSTRAINT pk_registroDeSubasta PRIMARY KEY (identificador),
    CONSTRAINT chk_importePagado CHECK (importe > 0.01),
    CONSTRAINT chk_comisionPagada CHECK (comision > 0.01),
    CONSTRAINT fk_registroDeSubasta_subastas FOREIGN KEY (subasta) REFERENCES subastas(identificador),
    CONSTRAINT fk_registroDeSubasta_duenios FOREIGN KEY (duenio) REFERENCES duenios(identificador),
    CONSTRAINT fk_registroDeSubasta_producto FOREIGN KEY (producto) REFERENCES productos(identificador),
    CONSTRAINT fk_registroDeSubasta_cliente FOREIGN KEY (cliente) REFERENCES clientes(identificador)
) ENGINE=InnoDB;
