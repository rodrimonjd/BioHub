-- Almacenamiento de eventos del historial (ASR-06, ASR-07).
-- Las dos tablas son SOLO DE INSERCIÓN: los triggers del final rechazan cualquier UPDATE o DELETE,
-- así ninguna versión ni cambio puede sobrescribirse ni borrarse.

-- Un registro por cada cambio recibido (creación, modificación, reclasificación, desactivación).
CREATE TABLE registro_cambio (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_evento      VARCHAR(36)   NOT NULL,
  tipo_registro  VARCHAR(20)   NOT NULL,          -- HALLAZGO o FICHA
  id_registro    VARCHAR(36)   NOT NULL,
  version        INT           NOT NULL,
  tipo_cambio    VARCHAR(20)   NOT NULL,          -- CREACION, MODIFICACION, RECLASIFICACION, DESACTIVACION
  campo          VARCHAR(100),
  valor_anterior VARCHAR(1000),
  valor_nuevo    VARCHAR(1000),
  motivo         VARCHAR(500),
  autor          VARCHAR(100)  NOT NULL,
  autorizador    VARCHAR(100),
  ocurrio_en     DATETIME(6)   NOT NULL,          -- cuándo ocurrió el cambio en el servicio de origen
  registrado_en  DATETIME(6)   NOT NULL,          -- cuándo lo guardó el historial
  carga_util     TEXT          NOT NULL,          -- evento original completo, tal como llegó
  hash_anterior  VARCHAR(64),
  hash           VARCHAR(64)   NOT NULL,          -- SHA-256 encadenado con el cambio anterior del mismo registro
  CONSTRAINT uq_cambio_evento  UNIQUE (id_evento),
  CONSTRAINT uq_cambio_version UNIQUE (tipo_registro, id_registro, version)
);

-- Estado completo del registro en cada versión (lo que se sabía en ese momento).
CREATE TABLE version_registro (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_evento     VARCHAR(36)  NOT NULL,
  tipo_registro VARCHAR(20)  NOT NULL,
  id_registro   VARCHAR(36)  NOT NULL,
  version       INT          NOT NULL,
  estado        TEXT,                              -- JSON; null si no se conoce la versión base
  autor         VARCHAR(100) NOT NULL,
  creada_en     DATETIME(6)  NOT NULL,
  CONSTRAINT uq_version_evento UNIQUE (id_evento),
  CONSTRAINT uq_version        UNIQUE (tipo_registro, id_registro, version)
);

CREATE TRIGGER registro_cambio_sin_update BEFORE UPDATE ON registro_cambio FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial es inmutable: no se permite modificar registro_cambio';

CREATE TRIGGER registro_cambio_sin_delete BEFORE DELETE ON registro_cambio FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial es inmutable: no se permite borrar registro_cambio';

CREATE TRIGGER version_registro_sin_update BEFORE UPDATE ON version_registro FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial es inmutable: no se permite modificar version_registro';

CREATE TRIGGER version_registro_sin_delete BEFORE DELETE ON version_registro FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial es inmutable: no se permite borrar version_registro';
