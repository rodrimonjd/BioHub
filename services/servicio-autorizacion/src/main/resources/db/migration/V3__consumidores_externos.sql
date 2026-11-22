-- Consumidores externos (universidades y ONG) que usan la API publica a traves del API Gateway.
-- El gateway autentica la API key y luego pregunta aqui si el consumidor esta autorizado.
INSERT INTO permisos (codigo, descripcion) VALUES
 ('api_externa:consultar', 'Consultar el inventario publico mediante la API externa');

INSERT INTO roles (nombre, descripcion) VALUES
 ('CONSUMIDOR_EXTERNO', 'Universidad u ONG que consume la API publica');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo IN ('hallazgo:ver', 'api_externa:consultar')
WHERE r.nombre = 'CONSUMIDOR_EXTERNO';

-- Mismos ids que los consumidores configurados en services/api-gateway (application.yml).
-- consumidor-ong-suspendida no recibe el rol a proposito: el gateway la autentica pero aqui se rechaza.
INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT 'consumidor-unal', id FROM roles WHERE nombre = 'CONSUMIDOR_EXTERNO';

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT 'consumidor-humboldt', id FROM roles WHERE nombre = 'CONSUMIDOR_EXTERNO';
