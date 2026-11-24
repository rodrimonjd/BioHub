-- Historial consultable de los cambios de roles: asignar o quitar un rol a una cuenta,
-- crear un rol y cambiar sus permisos. Es de solo inserción.
CREATE TABLE historial_roles (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  accion        VARCHAR(30)   NOT NULL,
  usuario_id    VARCHAR(100)  NULL,
  rol           VARCHAR(60)   NOT NULL,
  detalle       VARCHAR(2000) NULL,
  realizado_por VARCHAR(100)  NOT NULL,
  realizado_en  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX ix_historial_roles_fecha (realizado_en)
);

-- Los cambios hechos antes de esta migración solo estaban en evento_outbox, que no guardaba quién los hizo
INSERT INTO historial_roles (accion, usuario_id, rol, detalle, realizado_por, realizado_en)
SELECT
  CASE tipo
    WHEN 'usuario.rol_asignado'      THEN 'ROL_ASIGNADO'
    WHEN 'usuario.rol_revocado'      THEN 'ROL_QUITADO'
    WHEN 'rol.creado'                THEN 'ROL_CREADO'
    WHEN 'rol.permisos_actualizados' THEN 'PERMISOS_CAMBIADOS'
  END,
  JSON_UNQUOTE(JSON_EXTRACT(carga_util, '$.usuario')),
  JSON_UNQUOTE(JSON_EXTRACT(carga_util, '$.rol')),
  CAST(JSON_EXTRACT(carga_util, '$.permisos') AS CHAR(2000)),
  'sin registro',
  creado_en
FROM evento_outbox
WHERE tipo IN ('usuario.rol_asignado', 'usuario.rol_revocado', 'rol.creado', 'rol.permisos_actualizados')
ORDER BY id;

CREATE TRIGGER historial_roles_sin_update BEFORE UPDATE ON historial_roles FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial de roles es inmutable: no se permite modificarlo';

CREATE TRIGGER historial_roles_sin_delete BEFORE DELETE ON historial_roles FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial de roles es inmutable: no se permite borrarlo';
