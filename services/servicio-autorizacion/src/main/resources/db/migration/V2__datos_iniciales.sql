INSERT INTO permisos (codigo, descripcion) VALUES
 ('hallazgo:crear',        'Registrar hallazgos y avistamientos'),
 ('hallazgo:ver',          'Consultar hallazgos'),
 ('hallazgo:editar',       'Editar hallazgos propios o de su equipo'),
 ('hallazgo:validar',      'Validar o rechazar hallazgos'),
 ('evidencia:subir',       'Subir evidencias (fotos, audio, video)'),
 ('equipo:gestionar',      'Crear y gestionar equipos de campo'),
 ('ubicacion:ver_exacta',  'Ver coordenadas exactas sin restricción'),
 ('historial:ver',         'Consultar el historial de versiones'),
 ('reporte:ver',           'Ver reportes e índices de presión'),
 ('reporte:generar',       'Solicitar la generación de reportes'),
 ('roles:administrar',     'Administrar roles y permisos');

INSERT INTO roles (nombre, descripcion) VALUES
 ('ADMIN',               'Administrador del sistema'),
 ('AUTORIDAD_AMBIENTAL', 'Autoridad ambiental / curador de datos'),
 ('INVESTIGADOR',        'Investigador o biólogo de campo'),
 ('CIUDADANO',           'Público general (rol por defecto)');

-- ADMIN: todos los permisos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p WHERE r.nombre = 'ADMIN';

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo IN ('hallazgo:ver','hallazgo:validar','ubicacion:ver_exacta','historial:ver','reporte:ver','reporte:generar')
WHERE r.nombre = 'AUTORIDAD_AMBIENTAL';

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo IN ('hallazgo:crear','hallazgo:ver','hallazgo:editar','evidencia:subir','equipo:gestionar','historial:ver','reporte:ver')
WHERE r.nombre = 'INVESTIGADOR';

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo IN ('hallazgo:ver')
WHERE r.nombre = 'CIUDADANO';

-- Usuario administrador inicial (cambiar por el id real que emita el gateway)
INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT 'admin', id FROM roles WHERE nombre = 'ADMIN';
