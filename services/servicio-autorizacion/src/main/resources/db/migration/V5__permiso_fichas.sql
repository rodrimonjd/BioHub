-- Editar fichas taxonómicas (crear fichas, agregar o cambiar atributos, desactivarlas) en servicio-taxonomia.
-- No lo tienen CIUDADANO (invitado) ni CONSUMIDOR_EXTERNO: solo consultan.
INSERT INTO permisos (codigo, descripcion) VALUES
 ('ficha:editar', 'Crear y editar fichas taxonómicas y sus atributos');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo = 'ficha:editar'
WHERE r.nombre IN ('ADMIN', 'AUTORIDAD_AMBIENTAL', 'INVESTIGADOR');
