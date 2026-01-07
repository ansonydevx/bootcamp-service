CREATE TABLE IF NOT EXISTS bootcamps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    fecha_lanzamiento DATE NOT NULL,
    duracion INT NOT NULL
);


CREATE TABLE IF NOT EXISTS bootcamps_capacidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bootcamp_id BIGINT NOT NULL,
    capacidad_id BIGINT NOT NULL,
    CONSTRAINT uk_bootcamp_capacidad UNIQUE (bootcamp_id, capacidad_id),
    INDEX idx_bootcamp_id (bootcamp_id),
    INDEX idx_capacidad_id (capacidad_id)
);