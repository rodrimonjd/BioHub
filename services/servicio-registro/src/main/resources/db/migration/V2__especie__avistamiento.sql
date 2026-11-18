CREATE TABLE especie (
    id                  CHAR(36)     PRIMARY KEY,
    nombre_cientifico   VARCHAR(150) NOT NULL UNIQUE,
    nombre_comun        VARCHAR(150),
    estado_conservacion VARCHAR(30)  NOT NULL
);

CREATE TABLE avistamiento (
    id              CHAR(36)     PRIMARY KEY,
    investigador_id VARCHAR(36)  NOT NULL,
    especie_id      CHAR(36)     NOT NULL,
    fecha_hora      DATETIME(6)  NOT NULL,
    cantidad        INT          NOT NULL,
    latitud         DOUBLE,
    longitud        DOUBLE,
    departamento    VARCHAR(80)  NOT NULL,
    region          VARCHAR(80),
    creado_en       DATETIME(6)  NOT NULL,
    CONSTRAINT fk_avistamiento_especie FOREIGN KEY (especie_id) REFERENCES especie(id)
);

CREATE INDEX idx_avistamiento_fecha ON avistamiento(fecha_hora);