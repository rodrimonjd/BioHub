# servicio-historial

Historial **inmutable** de los registros científicos: cada cambio con autor, autorizador, valor anterior y nuevo, y el estado completo del registro en cada versión. Se suscribe al broker.

**ASR:** ASR-06 (trazabilidad), ASR-07 (conservación de versiones)
**Requisitos:** RF-09, RF-10, RNF-06, RNF-07
**Historias:** HU-09, HU-10, HU-19.1, HU-20.1 (y cubre HU-19.2, HU-19.3, HU-20.2, HU-20.3)
**Eventos que consume:** `HALLAZGO_REGISTRADO`, `HALLAZGO_MODIFICADO` (servicio-registro) · `FICHA_CREADA`, `FICHA_MODIFICADA`, `FICHA_DESACTIVADA` (servicio-taxonomia)
**Eventos que publica:** ninguno

## Tecnología
Java 21 · Spring Boot 4.1 · Spring Data JPA · MySQL 8 (`historial_db`) · Flyway · Spring AMQP (RabbitMQ) · Caffeine

## Cómo funciona
```
registro / taxonomía ──(outbox → RabbitMQ: biohub.eventos)──► cola historial.eventos
                                                                    │
                                     ConsumidorEventos → TraductorEventos → ServicioRegistroHistorial
                                                                    │  (ConstructorRegistroCambio valida y calcula el hash)
                                               historial_db: registro_cambio + version_registro (solo INSERT)
                                                                    │
GET /api/v1/historial/... ──► VerificadorPermisos (historial:ver en servicio-autorizacion) ──► ServicioConsultaHistorial
```

### Cómo se cumplen los ASR
| Requisito | Cómo |
|---|---|
| Registrar cada modificación (HU-19.1, ASR-06) | Cada evento se guarda como un `RegistroCambio` con el evento original completo (`carga_util`). |
| Responsable (HU-10, RF-10) | Siempre se guarda el autor; en una reclasificación, el `ConstructorRegistroCambio` **exige** autorizador y motivo. |
| Conservar versiones (HU-20.1, ASR-07) | Cada cambio crea una `VersionRegistro` nueva con el estado completo; las anteriores nunca se tocan. |
| No eliminada ni alterada (HU-19.3) | Triggers en MySQL rechazan todo `UPDATE` y `DELETE` (incluso como root) · entidades `@Immutable` · la API no tiene operaciones de escritura · cadena de hashes SHA-256 verificable en `/integridad`. |
| Sin cambios omitidos (HU-19.2) | Si llega una versión saltándose otras, se guarda igual y el historial lo indica (`completo: false`, `versionesFaltantes`). |
| Solo usuarios autorizados (HU-09, ASR-05) | Token de una cuenta con el permiso `historial:ver` (ADMIN, AUTORIDAD_AMBIENTAL, INVESTIGADOR). Si autorización no responde, se niega el acceso (503). |

### Robustez
- **Idempotente:** un evento repetido (mismo `idEvento`) o con una versión ya guardada se ignora.
- **Cola durable** `historial.eventos`: si el servicio está apagado, los eventos esperan en RabbitMQ.
- **Cola de errores** `historial.eventos.dlq`: eventos ilegibles o incompletos (p. ej. reclasificación sin autorizador) van directo, con el motivo en la cabecera `x-motivo-error`. Otros fallos (BD caída): 3 reintentos y luego dead-letter. Nada se pierde en silencio.

## Estructura
```
src/main/java/co/edu/unbosque/biohub/historial/
├── HistorialApplication.java
├── cambio/      RegistroCambio, ConstructorRegistroCambio (Builder), CalculadoraHash, TipoCambio, TipoRegistro
├── version/     VersionRegistro (estado completo por versión)
├── evento/      ConfiguracionBroker, ConsumidorEventos (Observer), TraductorEventos (Adapter), EventoEntrante
├── historial/   ServicioRegistroHistorial (guardar), ServicioConsultaHistorial (consultar), HistorialControlador, Vistas
├── permiso/     VerificadorPermisos, ClienteAutorizacion (Adapter) + ClienteAutorizacionHttp
└── compartido/  ManejadorExcepciones, RespuestaError y excepciones
src/main/resources/db/migration/   V1 (tablas de solo inserción + triggers)
```

## Ejecutar
```bash
docker compose -f infra/docker-compose.yml up -d --build   # puerto 8086
mvn test
```
Variables de entorno (con valores por defecto): `BD_HOST`, `BD_PUERTO`, `BD_NOMBRE` (`historial_db`), `BD_USUARIO` (`historial_user`), `BD_CLAVE` (`historial_pass`), `RABBIT_HOST`, `RABBIT_PUERTO`, `RABBIT_USUARIO`, `RABBIT_CLAVE`, `AUTORIZACION_URL` (`http://localhost:8082`), `PUERTO_SERVIDOR`.

> **MySQL y los triggers:** con el binlog activo, MySQL solo deja crear triggers a usuarios sin SUPER si `log_bin_trust_function_creators=1`. El `docker-compose.yml` ya arranca MySQL con esa opción. Si tu contenedor de MySQL es anterior, recréalo con `docker compose -f infra/docker-compose.yml up -d mysql`.

## API (`/api/v1/historial`)
`{tipo}` es `hallazgos` o `fichas`. Todas exigen un token (`Authorization: Bearer`) de una cuenta con el permiso `historial:ver`.

| Método | Ruta | Descripción | Historia |
|---|---|---|---|
| GET | `/{tipo}/{id}` | Cambios en orden cronológico con autor y autorizador; indica si no hay modificaciones o si faltan versiones | HU-09, HU-10, HU-19.2 |
| GET | `/{tipo}/{id}/cambios/{version}` | Detalle de un cambio: campo, valor anterior y nuevo, motivo, responsables | HU-09 |
| GET | `/{tipo}/{id}/versiones` | Versiones conservadas, ordenadas cronológicamente | HU-20.2, HU-20.3 |
| GET | `/{tipo}/{id}/versiones/{n}` | Estado completo del registro en la versión `n` ("qué se sabía en esa fecha") | HU-20.1 |
| GET | `/{tipo}/{id}/integridad` | Recalcula la cadena de hashes: detecta cambios alterados, borrados o faltantes | HU-19.3 |

No existen `POST`, `PUT` ni `DELETE`: el historial solo se alimenta de eventos.

## Ejemplos (PowerShell)
```powershell
$r = Invoke-RestMethod -Method Post -Uri http://localhost:8082/api/v1/autorizacion/sesiones -ContentType application/json -Body '{"usuario":"admin","clave":"BioHub-Admin-2026"}'
curl.exe http://localhost:8086/api/v1/historial/hallazgos/<id> -H "Authorization: Bearer $($r.token)"

curl.exe http://localhost:8086/api/v1/historial/hallazgos/<id>/versiones/1 -H "Authorization: Bearer $($r.token)"

curl.exe http://localhost:8086/api/v1/historial/fichas/<id>/integridad -H "Authorization: Bearer $($r.token)"
```

## Patrones
- **Builder:** `ConstructorRegistroCambio` no permite terminar un cambio sin autor, valores o (en reclasificación) autorizador y motivo.
- **Observer:** `ConsumidorEventos` es un suscriptor más de los eventos de registro y taxonomía.
- **Adapter:** `TraductorEventos` aísla el formato de los eventos; `ClienteAutorizacion` aísla la llamada HTTP a autorización.
- **Almacenamiento de eventos:** el evento original se conserva completo junto a cada cambio.
