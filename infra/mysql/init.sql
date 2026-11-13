CREATE DATABASE IF NOT EXISTS auth_db      CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS registro_db  CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS historial_db CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS reportes_db  CHARACTER SET utf8mb4;

CREATE USER IF NOT EXISTS 'auth_user'@'%'      IDENTIFIED BY 'auth_pass';
CREATE USER IF NOT EXISTS 'registro_user'@'%'  IDENTIFIED BY 'registro_pass';
CREATE USER IF NOT EXISTS 'historial_user'@'%' IDENTIFIED BY 'historial_pass';
CREATE USER IF NOT EXISTS 'reportes_user'@'%'  IDENTIFIED BY 'reportes_pass';

GRANT ALL PRIVILEGES ON auth_db.*      TO 'auth_user'@'%';
GRANT ALL PRIVILEGES ON registro_db.*  TO 'registro_user'@'%';
GRANT ALL PRIVILEGES ON historial_db.* TO 'historial_user'@'%';
GRANT ALL PRIVILEGES ON reportes_db.*  TO 'reportes_user'@'%';
FLUSH PRIVILEGES;