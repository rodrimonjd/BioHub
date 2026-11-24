# servicio-autorizacion

Roles y permisos centralizados; decide si se muestran coordenadas exactas o solo región/departamento.

**ASR:** ASR-04, ASR-05
**Eventos que publica:** `rol.creado`, `rol.permisos_actualizados`, `usuario.rol_asignado`, `usuario.rol_revocado` (mediante la tabla `evento_outbox`)
**Eventos que consume:** ninguno por ahora

## Tecnología
Java 17 · Spring Boot 4.1 · Spring Data JPA · MySQL 8 · Flyway

## Estructura
```
src/main/java/co/edu/unbosque/biohub/autorizacion/
├── AutorizacionServiceApplication.java
├── autorizacion/   ServicioAutorizacion, AutorizacionControlador (consultas)
├── rol/            Rol, RolRepositorio, RolServicio, RolControlador (administración)
├── permiso/        Permiso, PermisoRepositorio
├── usuario/        UsuarioRol, UsuarioRolRepositorio
├── evento/         EventoOutbox, EventoOutboxRepositorio (patrón Outbox)
├── visibilidad/    PoliticaVisibilidadUbicacion (patrón Strategy), NivelVisibilidad
└── compartido/     Dtos, excepciones y ManejadorExcepciones
src/main/resources/db/migration/   V1 (esquema), V2 (datos iniciales)
```

## Ejecutar
```bash
# Con MySQL de infra/ en marcha:
mvn spring-boot:run          # puerto 8082
mvn test
```
Variables de entorno (con valores por defecto): `BD_HOST`, `BD_PUERTO`, `BD_NOMBRE` (`auth_db`), `BD_USUARIO` (`auth_user`), `BD_CLAVE` (`auth_pass`), `PUERTO_SERVIDOR`.
La base `auth_db` y el usuario `auth_user` los crea `infra/mysql/init.sql` al iniciar el contenedor de MySQL.

Flyway crea el esquema y los datos iniciales (roles: ADMIN, AUTORIDAD_AMBIENTAL, INVESTIGADOR, CIUDADANO; usuario `admin` con rol ADMIN).

## API (`/api/v1/autorizacion`)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/verificar` | `{usuarioId, permiso}` → `{permitido}` |
| GET | `/usuarios/{usuarioId}/permisos` | Roles y permisos efectivos |
| POST | `/consumidores/validar` | `{consumidorId}` → `{valido}`. Lo usa el API Gateway: es válido si el consumidor tiene el permiso `api_externa:consultar` (rol `CONSUMIDOR_EXTERNO`, migración V3) |
| POST | `/visibilidad-ubicacion` | `{usuarioId?, categoriaConservacion}` → `EXACTA` / `REGION` / `DEPARTAMENTO` |
| GET/POST | `/roles` | Listar / crear roles (admin) |
| PUT | `/roles/{nombre}/permisos` | Reemplazar los permisos de un rol (admin) |
| POST/DELETE | `/usuarios/{usuarioId}/roles/{nombreRol}` | Asignar / revocar rol (admin) |
| POST | `/sesiones` | Inicio de sesión: `{usuario (id o correo), clave}` → `{usuarioId, nombre, correo, roles, permisos}`. 401 con un mensaje genérico si no coinciden |
| GET/POST | `/usuarios` | Listar / crear cuentas `{id, nombre, correo, clave, rol?}` (admin) |
| GET | `/usuarios/con-permiso/{permiso}` | Cuentas activas con un permiso (solo id y nombre), p. ej. `hallazgo:validar` para elegir quién autoriza una reclasificación. Exige `X-Usuario-Id` de una cuenta activa |

Los endpoints de administración exigen la cabecera `X-Usuario-Id` (la pone el API Gateway) con el permiso `roles:administrar`.

## Cuentas e inicio de sesión
La identidad y el acceso están centralizados en este servicio (ASR-05): la tabla `usuarios` (migración V4) guarda cada cuenta y `usuario_roles` sus roles. El id de la cuenta es el mismo `X-Usuario-Id` que reciben los demás servicios.

- Las contraseñas se guardan **solo como hash BCrypt** (`spring-security-crypto`), nunca en texto plano, y la respuesta nunca las incluye.
- Usuario inexistente, cuenta inactiva o contraseña incorrecta dan el mismo 401 *"Usuario o contraseña incorrectos"*, y se compara un hash aunque el usuario no exista, para no revelar qué cuentas existen.
- La interfaz web (`services/web-app`) inicia sesión con `/sesiones` y muestra u oculta cada acción según los **permisos** recibidos (`hallazgo:crear`, `hallazgo:validar`, `ubicacion:ver_exacta`, `historial:ver`...).

Cuentas iniciales **solo para desarrollo** (cámbialas en cualquier entorno real):

| Usuario | Correo | Contraseña | Rol |
|---|---|---|---|
| `admin` | admin@biohub.gov.co | `BioHub-Admin-2026` | ADMIN |
| `investigador.campo` | investigador@biohub.gov.co | `BioHub-Invest-2026` | INVESTIGADOR |
| `autoridad.ambiental` | autoridad@biohub.gov.co | `BioHub-Autoridad-2026` | AUTORIDAD_AMBIENTAL |

## Ejemplos (PowerShell)
```powershell
curl.exe -X POST http://localhost:8082/api/v1/autorizacion/verificar -H "Content-Type: application/json" -d '{\"usuarioId\":\"admin\",\"permiso\":\"roles:administrar\"}'

curl.exe -X POST http://localhost:8082/api/v1/autorizacion/visibilidad-ubicacion -H "Content-Type: application/json" -d '{\"categoriaConservacion\":\"CR\"}'

curl.exe -X POST http://localhost:8082/api/v1/autorizacion/usuarios/u123/roles/INVESTIGADOR -H "X-Usuario-Id: admin"
```

## Patrones
- **Strategy:** `PoliticaVisibilidadUbicacion` (reglas de visibilidad de la ubicación)
- **Outbox:** `RolServicio` guarda los eventos en `evento_outbox` dentro de la misma transacción
