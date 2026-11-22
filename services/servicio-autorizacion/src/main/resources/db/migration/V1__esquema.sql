CREATE TABLE permisos (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo      VARCHAR(100) NOT NULL UNIQUE,
  descripcion VARCHAR(255)
);

CREATE TABLE roles (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(60) NOT NULL UNIQUE,
  descripcion VARCHAR(255)
);

CREATE TABLE rol_permisos (
  rol_id     BIGINT NOT NULL,
  permiso_id BIGINT NOT NULL,
  PRIMARY KEY (rol_id, permiso_id),
  CONSTRAINT fk_rp_rol     FOREIGN KEY (rol_id)     REFERENCES roles(id)    ON DELETE CASCADE,
  CONSTRAINT fk_rp_permiso FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE
);

CREATE TABLE usuario_roles (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id VARCHAR(100) NOT NULL,
  rol_id     BIGINT NOT NULL,
  UNIQUE KEY uq_usuario_rol (usuario_id, rol_id),
  CONSTRAINT fk_ur_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Patrón Outbox: los eventos se guardan en la misma transacción que el cambio
CREATE TABLE evento_outbox (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_agregado VARCHAR(100) NOT NULL,
  tipo        VARCHAR(100) NOT NULL,
  carga_util  TEXT NOT NULL,
  creado_en   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  publicado   BOOLEAN NOT NULL DEFAULT FALSE
);
