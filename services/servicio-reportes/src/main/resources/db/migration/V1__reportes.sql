-- Copia de los avistamientos que publica servicio-registro (BD analitica).
CREATE TABLE avistamiento (
    id           CHAR(36)     PRIMARY KEY,
    especie_id   CHAR(36)     NOT NULL,
    especie      VARCHAR(150),
    fecha_hora   DATETIME(6)  NOT NULL,
    cantidad     INT          NOT NULL,
    departamento VARCHAR(80)  NOT NULL,
    region       VARCHAR(80),
    recibido_en  DATETIME(6)  NOT NULL
);

CREATE INDEX idx_avistamiento_fecha ON avistamiento(fecha_hora);

CREATE TABLE reporte (
    id                  CHAR(36)     PRIMARY KEY,
    tipo                VARCHAR(30)  NOT NULL,
    fecha_inicio        DATE         NOT NULL,
    fecha_fin           DATE         NOT NULL,
    departamento        VARCHAR(80),
    especie_id          CHAR(36),
    umbral              DOUBLE       NOT NULL,
    estado              VARCHAR(20)  NOT NULL,
    solicitado_por      VARCHAR(100) NOT NULL,
    mensaje             VARCHAR(500),
    total_avistamientos INT          NOT NULL,
    total_individuos    INT          NOT NULL,
    creado_en           DATETIME(6)  NOT NULL,
    generado_en         DATETIME(6)
);

CREATE INDEX idx_reporte_estado ON reporte(estado, creado_en);

CREATE TABLE indice_presion (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporte_id     CHAR(36)     NOT NULL,
    ecosistema     VARCHAR(170) NOT NULL,
    valor          DOUBLE       NOT NULL,
    supera_umbral  BOOLEAN      NOT NULL,
    avistamientos  INT          NOT NULL,
    individuos     INT          NOT NULL,
    fecha_calculo  DATETIME(6)  NOT NULL,
    CONSTRAINT fk_indice_reporte FOREIGN KEY (reporte_id) REFERENCES reporte(id)
);
