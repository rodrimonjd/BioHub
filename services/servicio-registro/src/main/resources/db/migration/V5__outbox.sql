CREATE TABLE outbox_evento (
    id           CHAR(36)      PRIMARY KEY,
    tipo         VARCHAR(50)   NOT NULL,
    id_agregado  VARCHAR(36)   NOT NULL,
    ocurrio_en   DATETIME(6)   NOT NULL,
    payload      VARCHAR(4000) NOT NULL,
    publicado    BOOLEAN       NOT NULL DEFAULT FALSE,
    publicado_en DATETIME(6),
    intentos     INT           NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_pendientes ON outbox_evento(publicado, ocurrio_en);