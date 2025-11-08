-- V1__Initial_schema.sql
-- Primera migración: Creación del esquema base de la base de datos

-- Tabla de usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de tipos de evento
CREATE TABLE tipos_evento (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT
);

-- Tabla de eventos
CREATE TABLE eventos (
    id BIGSERIAL PRIMARY KEY,
    id_externo BIGINT UNIQUE,
    titulo VARCHAR(255) NOT NULL,
    resumen TEXT,
    descripcion TEXT,
    fecha TIMESTAMP NOT NULL,
    direccion VARCHAR(500),
    imagen VARCHAR(1000),
    fila_asientos INTEGER NOT NULL,
    columna_asientos INTEGER NOT NULL,
    precio_entrada DECIMAL(10,2) NOT NULL,
    tipo_evento_id BIGINT REFERENCES tipos_evento(id),
    ultima_sincronizacion TIMESTAMP,
    activo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de integrantes de eventos
CREATE TABLE integrantes (
    id BIGSERIAL PRIMARY KEY,
    evento_id BIGINT REFERENCES eventos(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    identificacion VARCHAR(50)
);

-- Tabla de ventas
CREATE TABLE ventas (
    id BIGSERIAL PRIMARY KEY,
    id_externo BIGINT,
    usuario_id BIGINT REFERENCES usuarios(id),
    evento_id BIGINT REFERENCES eventos(id),
    fecha_venta TIMESTAMP NOT NULL,
    precio_total DECIMAL(10,2) NOT NULL,
    resultado BOOLEAN NOT NULL,
    descripcion TEXT,
    confirmada_catedra BOOLEAN DEFAULT false,
    intentos_sincronizacion INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de asientos de venta
CREATE TABLE asientos_venta (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT REFERENCES ventas(id) ON DELETE CASCADE,
    fila INTEGER NOT NULL,
    columna INTEGER NOT NULL,
    nombre_persona VARCHAR(200) NOT NULL,
    estado VARCHAR(50)
);

-- Índices para mejorar performance
CREATE INDEX idx_eventos_fecha ON eventos(fecha);
CREATE INDEX idx_eventos_activo ON eventos(activo);
CREATE INDEX idx_eventos_tipo ON eventos(tipo_evento_id);
CREATE INDEX idx_integrantes_evento ON integrantes(evento_id);
CREATE INDEX idx_ventas_usuario ON ventas(usuario_id);
CREATE INDEX idx_ventas_evento ON ventas(evento_id);
CREATE INDEX idx_ventas_confirmada ON ventas(confirmada_catedra);
CREATE INDEX idx_asientos_venta ON asientos_venta(venta_id);

-- Datos iniciales: tipos de evento comunes
INSERT INTO tipos_evento (nombre, descripcion) VALUES
    ('Conferencia', 'Evento de conferencia o charla'),
    ('Obra de teatro', 'Representación teatral'),
    ('Curso', 'Curso o taller'),
    ('Concierto', 'Evento musical'),
    ('Otro', 'Otro tipo de evento');

-- Comentarios en las tablas
COMMENT ON TABLE usuarios IS 'Usuarios del sistema que pueden comprar entradas';
COMMENT ON TABLE tipos_evento IS 'Tipos de eventos disponibles';
COMMENT ON TABLE eventos IS 'Eventos disponibles para venta de entradas';
COMMENT ON TABLE integrantes IS 'Integrantes o presentadores de eventos';
COMMENT ON TABLE ventas IS 'Registro de ventas de entradas';
COMMENT ON TABLE asientos_venta IS 'Asientos vendidos en cada venta';

