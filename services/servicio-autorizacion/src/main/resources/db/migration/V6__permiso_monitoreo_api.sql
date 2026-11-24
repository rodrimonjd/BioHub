-- Ver el consumo de la API externa (panel "Plataforma abierta con límite de peticiones").
-- El público general (CIUDADANO, también el invitado) no lo tiene.
INSERT INTO permisos (codigo, descripcion) VALUES
 ('api_externa:monitorear', 'Ver el consumo y el límite de peticiones de la API externa');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo = 'api_externa:monitorear'
WHERE r.nombre IN ('ADMIN', 'AUTORIDAD_AMBIENTAL', 'INVESTIGADOR');
