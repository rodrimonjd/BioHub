-- Cada expedicion es liderada por un investigador (diagrama de clases: Investigador 1 -- lidera -- 0..* Expedicion).
ALTER TABLE expedicion ADD COLUMN lider_id VARCHAR(36) NULL;

-- Las expediciones creadas antes de esta migracion no tenian lider.
UPDATE expedicion SET lider_id = 'sin-asignar' WHERE lider_id IS NULL;

ALTER TABLE expedicion MODIFY lider_id VARCHAR(36) NOT NULL;

CREATE INDEX idx_expedicion_lider ON expedicion(lider_id);
