-- El investigador también genera reportes (HU-12.2: "como investigador o administrador").
-- Ya tenía reporte:ver, que permite consultarlos y exportarlos.
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p
 ON p.codigo = 'reporte:generar'
WHERE r.nombre = 'INVESTIGADOR';
