CREATE TABLE equipo_campo (
    id             CHAR(36)     PRIMARY KEY,
    numero_serie   VARCHAR(50)  NOT NULL UNIQUE,
    tipo           VARCHAR(50)  NOT NULL,
    modelo         VARCHAR(100),
    estado         VARCHAR(20)  NOT NULL,
    responsable_id VARCHAR(36)
);

CREATE TABLE expedicion (
    id           CHAR(36)     PRIMARY KEY,
    nombre       VARCHAR(150) NOT NULL,
    zona         VARCHAR(150) NOT NULL,
    fecha_inicio DATE         NOT NULL,
    fecha_fin    DATE,
    creado_en    DATETIME(6)  NOT NULL
);

CREATE TABLE expedicion_equipo (
    expedicion_id CHAR(36) NOT NULL,
    equipo_id     CHAR(36) NOT NULL,
    PRIMARY KEY (expedicion_id, equipo_id),
    FOREIGN KEY (expedicion_id) REFERENCES expedicion(id),
    FOREIGN KEY (equipo_id)     REFERENCES equipo_campo(id)
);