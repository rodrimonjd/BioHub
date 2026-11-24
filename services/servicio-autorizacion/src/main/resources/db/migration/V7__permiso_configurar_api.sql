INSERT INTO permisos (codigo, descripcion) VALUES
 ('api_externa:configurar', 'Cambiar el límite de peticiones por minuto de la API externa');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo = 'api_externa:configurar'
WHERE r.nombre = 'ADMIN';
