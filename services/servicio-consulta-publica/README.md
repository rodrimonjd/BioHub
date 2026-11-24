# servicio-consulta-publica

Modelo de lectura con caché para consultar hallazgos. Muestra coordenadas exactas, región o solo departamento según lo que decida `servicio-autorizacion`. Se suscribe al broker.

**ASR:** ASR-01, ASR-02, ASR-04, ASR-05
**Historias:** HU-05 (consultar hallazgos), HU-06 (consultar ubicación), HU-07 (coordenadas exactas)
**Eventos que consume:** `HALLAZGO_REGISTRADO`, `HALLAZGO_MODIFICADO` (de `servicio-registro`, exchange `biohub.eventos`)
**Eventos que publica:** ninguno

## Tecnología
Java 21 · Spring Boot 4.1 · Spring Data JPA · MySQL 8 (`consulta_db`) · Flyway · Spring AMQP (RabbitMQ) · Caffeine

## Cómo funciona
```
servicio-registro ──(outbox → RabbitMQ: biohub.eventos)──► cola consulta-publica.hallazgos
                                                                │
                                                     ConsumidorEventos → ProyectorHallazgos
                                                                │
                                                         consulta_db.hallazgo_lectura
                                                                │
web-app y API Gateway
GET /api/v1/consulta/hallazgos ──► ServicioConsulta ──┬── CatalogoEspecies (GET /especies de registro, caché 5 min)
                                                      └── ClienteAutorizacion (POST /visibilidad-ubicacion, caché 60 s)
```
- La cola es durable: si el servicio está apagado, los eventos esperan en RabbitMQ y se aplican al arrancar.
- El proyector es idempotente: un evento con versión menor o igual a la guardada se ignora (el log dice "ignorado").
- Cola de errores `consulta-publica.hallazgos.dlq`:
  - JSON ilegible o datos inválidos (falta la fecha, número mal escrito...): va directo, sin reintentos, con la cabecera `x-motivo-error`.
  - Otros fallos (p. ej. base de datos caída): 3 reintentos y, si sigue fallando, RabbitMQ lo mueve allí (dead-letter). Nada se pierde.
- El modelo de lectura no guarda el investigador; solo datos públicos.
- Si `servicio-autorizacion` no responde, la ubicación se muestra al nivel más restrictivo (`DEPARTAMENTO`).

## Estructura
```
src/main/java/co/edu/unbosque/biohub/consulta/
├── ConsultaPublicaApplication.java
├── consulta/      ConsultaControlador, ServicioConsulta, PresentadorUbicacion, VistaHallazgoPublico
├── evento/        ConfiguracionBroker, ConsumidorEventos (Observer), ProyectorHallazgos
├── lectura/       HallazgoLectura, HallazgoLecturaRepositorio (modelo de lectura)
├── especie/       CatalogoEspecies (Adapter) + CatalogoEspeciesHttp
├── visibilidad/   ClienteAutorizacion + ClienteAutorizacionHttp, NivelVisibilidad
└── compartido/    ManejadorExcepciones, RespuestaError, NoEncontradoException
src/main/resources/db/migration/   V1 (tabla hallazgo_lectura)
```

## Ejecutar
```bash
docker compose -f infra/docker-compose.yml up -d --build   # puerto 8083
mvn test
```
Variables de entorno (con valores por defecto): `BD_HOST`, `BD_PUERTO`, `BD_NOMBRE` (`consulta_db`), `BD_USUARIO` (`consulta_user`), `BD_CLAVE` (`consulta_pass`), `RABBIT_HOST`, `RABBIT_PUERTO`, `RABBIT_USUARIO`, `RABBIT_CLAVE`, `REGISTRO_URL` (`http://localhost:8081`), `AUTORIZACION_URL` (`http://localhost:8082`), `PUERTO_SERVIDOR`.

> Si tu volumen de MySQL ya existía antes de agregar `consulta_db` a `infra/mysql/init.sql`, créala a mano (el script solo corre con el volumen vacío):
> ```powershell
> docker exec biohub-mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS consulta_db CHARACTER SET utf8mb4; CREATE USER IF NOT EXISTS 'consulta_user'@'%' IDENTIFIED BY 'consulta_pass'; GRANT ALL PRIVILEGES ON consulta_db.* TO 'consulta_user'@'%';"
> ```

> Si el servicio falla al arrancar con `PRECONDITION_FAILED - inequivalent arg 'x-dead-letter-exchange'`, tienes una cola `consulta-publica.hallazgos` creada por una versión anterior (sin cola de errores). Detén el servicio, bórrala y vuelve a arrancarlo:
> ```powershell
> docker stop biohub-servicio-consulta-publica
> docker exec biohub-rabbitmq rabbitmqctl delete_queue consulta-publica.hallazgos
> docker start biohub-servicio-consulta-publica
> ```
>
> Para ver qué hay en la cola de errores: http://localhost:15672 (usuario `biohub` / `biohub`) → Queues → `consulta-publica.hallazgos.dlq` → Get messages.

## API (`/api/v1/consulta`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/hallazgos` | Listado paginado. Filtros opcionales: `especieId`, `departamento`, `especie` (texto en el nombre científico o común), `texto` (palabras clave, hasta 100 caracteres), `fechaDesde` y `fechaHasta` (`AAAA-MM-DD`, incluidas), `pagina` (0), `tamano` (20, máx. 100) |
| GET | `/hallazgos/{id}` | Un hallazgo |

La palabra clave busca cada palabra en la especie (científica o común), la descripción y el departamento, sin distinguir tildes ni mayúsculas. No busca en la región ni en las coordenadas, para que no se pueda deducir una ubicación protegida.

Token opcional (`Authorization: Bearer`, lo ponen el API Gateway y la web). Sin él la consulta es anónima; con uno falso o vencido responde 401.

### Visibilidad de la ubicación (`nivelUbicacion`)
| Nivel | Coordenadas | Región | Departamento |
|---|---|---|---|
| `EXACTA` | ✅ | ✅ | ✅ |
| `REGION` | — | ✅ | ✅ |
| `DEPARTAMENTO` | — | — | ✅ |

Con las reglas actuales de `servicio-autorizacion`: quien no tiene permiso nunca ve coordenadas (solo departamento en CR/EN y región en las demás); quien tiene `ubicacion:ver_exacta` (ADMIN, AUTORIDAD_AMBIENTAL) siempre ve coordenadas.

## Ejemplos (PowerShell)
```powershell
curl.exe "http://localhost:8083/api/v1/consulta/hallazgos?departamento=Meta"

curl.exe "http://localhost:8083/api/v1/consulta/hallazgos?especie=jaguar&texto=meta&fechaDesde=2026-11-01"

$r = Invoke-RestMethod -Method Post -Uri http://localhost:8082/api/v1/autorizacion/sesiones -ContentType application/json -Body '{"usuario":"admin","clave":"BioHub-Admin-2026"}'
curl.exe http://localhost:8083/api/v1/consulta/hallazgos/<id> -H "Authorization: Bearer $($r.token)"
```

## Patrones
- **Observer:** `ConsumidorEventos` se suscribe a los eventos de registro.
- **Adapter:** `CatalogoEspecies` y `ClienteAutorizacion` aíslan las llamadas HTTP a otros servicios.
- **Modelo de lectura (CQRS):** `hallazgo_lectura` es una copia optimizada para consultas.
