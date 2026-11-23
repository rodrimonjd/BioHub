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

Los endpoints de administración exigen la cabecera `X-Usuario-Id` (la pone el API Gateway) con el permiso `roles:administrar`.

## Ejemplos (PowerShell)
```powershell
curl.exe -X POST http://localhost:8082/api/v1/autorizacion/verificar -H "Content-Type: application/json" -d '{\"usuarioId\":\"admin\",\"permiso\":\"roles:administrar\"}'

curl.exe -X POST http://localhost:8082/api/v1/autorizacion/visibilidad-ubicacion -H "Content-Type: application/json" -d '{\"categoriaConservacion\":\"CR\"}'

curl.exe -X POST http://localhost:8082/api/v1/autorizacion/usuarios/u123/roles/INVESTIGADOR -H "X-Usuario-Id: admin"
```

## Patrones
- **Strategy:** `PoliticaVisibilidadUbicacion` (reglas de visibilidad de la ubicación)
- **Outbox:** `RolServicio` guarda los eventos en `evento_outbox` dentro de la misma transacción
