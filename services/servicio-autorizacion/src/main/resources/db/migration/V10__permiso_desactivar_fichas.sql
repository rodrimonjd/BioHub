-- Desactivar fichas taxonómicas tiene su propio permiso, separado de editarlas (ficha:editar).
-- La ficha no se borra: queda inactiva y el cambio se guarda en el historial.
INSERT INTO permisos (codigo, descripcion) VALUES
 ('ficha:desactivar', 'Desactivar fichas taxonómicas');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo = 'ficha:desactivar'
WHERE r.nombre IN ('ADMIN', 'AUTORIDAD_AMBIENTAL', 'INVESTIGADOR');
