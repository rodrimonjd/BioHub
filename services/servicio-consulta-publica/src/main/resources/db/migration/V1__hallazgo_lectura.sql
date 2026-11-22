-- Modelo de lectura: copia desnormalizada de los hallazgos, alimentada por los eventos de servicio-registro.
-- No guarda el investigador: este servicio solo expone información pública.
CREATE TABLE hallazgo_lectura (
  id             VARCHAR(36)   NOT NULL PRIMARY KEY,
  especie_id     VARCHAR(36),
  especie        VARCHAR(150)  NOT NULL,
  fecha          DATETIME(6)   NOT NULL,
  descripcion    VARCHAR(1000) NOT NULL,
  latitud        DOUBLE,
  longitud       DOUBLE,
  departamento   VARCHAR(80)   NOT NULL,
  region         VARCHAR(80),
  version        INT           NOT NULL,
  actualizado_en DATETIME(6)   NOT NULL
);

CREATE INDEX idx_lectura_departamento ON hallazgo_lectura (departamento);
CREATE INDEX idx_lectura_especie      ON hallazgo_lectura (especie_id);
CREATE INDEX idx_lectura_fecha        ON hallazgo_lectura (fecha);
