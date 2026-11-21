CREATE TABLE hallazgo (
    id              CHAR(36)      PRIMARY KEY,
    investigador_id VARCHAR(36)   NOT NULL,
    especie_id      CHAR(36)      NOT NULL,
    ficha_id        VARCHAR(36),
    fecha           DATETIME(6)   NOT NULL,
    descripcion     VARCHAR(1000) NOT NULL,
    latitud         DOUBLE,
    longitud        DOUBLE,
    departamento    VARCHAR(80)   NOT NULL,
    region          VARCHAR(80),
    version_actual  INT           NOT NULL,
    creado_en       DATETIME(6)   NOT NULL,
    CONSTRAINT fk_hallazgo_especie FOREIGN KEY (especie_id) REFERENCES especie(id)
);

CREATE TABLE evidencia (
    id                  CHAR(36)     PRIMARY KEY,
    hallazgo_id         CHAR(36)     NOT NULL,
    tipo                VARCHAR(20)  NOT NULL,
    valor               VARCHAR(500),
    unidad              VARCHAR(20),
    url_archivo         VARCHAR(500),
    atributo_taxonomico VARCHAR(100),
    CONSTRAINT fk_evidencia_hallazgo FOREIGN KEY (hallazgo_id) REFERENCES hallazgo(id)
);

CREATE INDEX idx_hallazgo_fecha ON hallazgo(fecha);
CREATE INDEX idx_evidencia_hallazgo ON evidencia(hallazgo_id);