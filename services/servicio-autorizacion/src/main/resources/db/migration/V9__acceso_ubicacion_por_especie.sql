-- La autoridad ambiental decide quién ve la ubicación exacta y de qué especies.
INSERT INTO permisos (codigo, descripcion) VALUES
 ('ubicacion:administrar', 'Dar o quitar a un usuario el acceso a la ubicación exacta de una especie');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo = 'ubicacion:administrar'
WHERE r.nombre IN ('ADMIN', 'AUTORIDAD_AMBIENTAL');

-- Accesos vigentes: el usuario ve las coordenadas exactas de esa especie aunque no tenga ubicacion:ver_exacta.
CREATE TABLE acceso_ubicacion (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id   VARCHAR(100) NOT NULL,
  especie_id   VARCHAR(36)  NOT NULL,
  otorgado_por VARCHAR(100) NOT NULL,
  otorgado_en  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_acceso_ubicacion (usuario_id, especie_id)
);

-- Historial de cada acceso que se da o se quita. Es de solo inserción.
CREATE TABLE historial_acceso_ubicacion (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id    VARCHAR(100) NOT NULL,
  especie_id    VARCHAR(36)  NOT NULL,
  accion        VARCHAR(20)  NOT NULL,
  realizado_por VARCHAR(100) NOT NULL,
  realizado_en  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX ix_historial_acceso_fecha (realizado_en)
);

CREATE TRIGGER historial_acceso_ubicacion_sin_update BEFORE UPDATE ON historial_acceso_ubicacion FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial de accesos a ubicaciones es inmutable: no se permite modificarlo';

CREATE TRIGGER historial_acceso_ubicacion_sin_delete BEFORE DELETE ON historial_acceso_ubicacion FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial de accesos a ubicaciones es inmutable: no se permite borrarlo';
