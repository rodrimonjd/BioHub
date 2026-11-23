-- El reporte ahora calcula todos los indices (uno por tipo) para cada ecosistema
-- y guarda un resumen por especie.
ALTER TABLE reporte DROP COLUMN tipo, DROP COLUMN umbral;

ALTER TABLE indice_presion
    ADD COLUMN tipo   VARCHAR(30) NOT NULL DEFAULT 'DENSIDAD_OBSERVACION',
    ADD COLUMN umbral DOUBLE      NOT NULL DEFAULT 0;

CREATE TABLE resumen_especie (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporte_id    CHAR(36)     NOT NULL,
    especie_id    CHAR(36)     NOT NULL,
    especie       VARCHAR(150),
    avistamientos INT          NOT NULL,
    individuos    INT          NOT NULL,
    CONSTRAINT fk_resumen_reporte FOREIGN KEY (reporte_id) REFERENCES reporte(id)
);
