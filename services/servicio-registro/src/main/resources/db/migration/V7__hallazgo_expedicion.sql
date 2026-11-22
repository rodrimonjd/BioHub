ALTER TABLE hallazgo ADD COLUMN expedicion_id CHAR(36) NULL;

ALTER TABLE hallazgo
    ADD CONSTRAINT fk_hallazgo_expedicion
    FOREIGN KEY (expedicion_id) REFERENCES expedicion(id);