-- Cuentas de usuario del panel científico. La identidad y el acceso quedan centralizados en este servicio (ASR-05):
-- aquí están las cuentas con su contraseña y en usuario_roles sus roles. El id es el mismo usuario_id de usuario_roles
-- y el que los demás servicios reciben en la cabecera X-Usuario-Id.
-- La contraseña se guarda solo como hash BCrypt, nunca en texto plano.
CREATE TABLE usuarios (
  id         VARCHAR(100) PRIMARY KEY,
  nombre     VARCHAR(150) NOT NULL,
  correo     VARCHAR(150) NOT NULL UNIQUE,
  hash_clave VARCHAR(100) NOT NULL,
  activo     BOOLEAN      NOT NULL DEFAULT TRUE,
  creado_en  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cuentas iniciales para desarrollo, una por rol (contraseñas documentadas en el README del servicio).
-- 'admin' ya tenía el rol ADMIN desde V2.
INSERT INTO usuarios (id, nombre, correo, hash_clave) VALUES
 ('admin',               'Administrador del sistema', 'admin@biohub.gov.co',
  '$2a$10$QF1R5tYN86q1dgLIORkLZOIWABYSA1WlvMbOmphfw1Q1K7Va9uZFe'),
 ('investigador.campo',  'Investigador de campo',     'investigador@biohub.gov.co',
  '$2a$10$qadMheVlq6.dLxgk/cOnpuBe4udiSflnVlaaU5Z0Rmn/w3KtCK9la'),
 ('autoridad.ambiental', 'Autoridad ambiental',       'autoridad@biohub.gov.co',
  '$2a$10$2Ou1eMQ8oc9ELE9FxCXhbu6PrPqlgJtFMlVZCtDqdWamwgaaDC5/y');

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT 'investigador.campo', id FROM roles WHERE nombre = 'INVESTIGADOR';
INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT 'autoridad.ambiental', id FROM roles WHERE nombre = 'AUTORIDAD_AMBIENTAL';
