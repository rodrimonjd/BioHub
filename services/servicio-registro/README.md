# servicio-registro

Especies, hallazgos con sus evidencias, expediciones, equipos de campo y avistamientos. Publica eventos con patrón Outbox. Las fichas taxonómicas están en `servicio-taxonomia`.

**ASR:** ASR-04, ASR-05, ASR-06, ASR-08, ASR-09
**Historias:** HU-01 (registrar hallazgos), HU-03 (evidencias), HU-04 (equipos de campo), HU-10 (responsable de cada cambio), HU-13 (avistamientos diarios), HU-19.1 (registrar modificaciones)

## Tecnología
Java 21 · Spring Boot 4.1 · Spring Data JPA · MySQL 8 (`registro_db`) · Flyway · Spring AMQP (RabbitMQ) · Puerto 8081

## API
| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/especies?nombre=` | Busca especies por nombre |
| POST | `/especies` | Agrega una especie |
| GET | `/hallazgos?investigadorId=` | Lista hallazgos |
| GET | `/hallazgos/{id}` | Un hallazgo con sus evidencias |
| POST | `/hallazgos` | Registra un hallazgo, con expedición, ficha y evidencias opcionales (HU-01) |
| PATCH | `/hallazgos/{id}` | Modifica un dato del hallazgo (HU-19.1) |
| POST | `/hallazgos/{id}/evidencias` | Agrega una evidencia: `TEMPERATURA`, `ADN` o `AUDIO`; el audio se guarda como enlace (HU-03) |
| POST | `/hallazgos/{id}/reclasificacion` | Cambia la especie con motivo y autorizador |
| GET/POST | `/avistamientos` | Lista / registra avistamientos (HU-13) |
| GET | `/avistamientos/{id}` | Un avistamiento |
| GET/POST | `/equipos` | Lista / registra equipos de campo (HU-04) |
| GET | `/equipos/{id}` | Un equipo |
| PUT | `/equipos/{id}/responsable` | Cambia el investigador responsable |
| GET/POST | `/expediciones` | Lista (filtro `liderId`) / crea expediciones; cada una trae sus equipos (HU-04) |
| GET | `/expediciones/{id}` | Una expedición con sus equipos |
| POST | `/expediciones/{id}/equipos` | Asigna un equipo; el líder queda como responsable |

Swagger: http://localhost:8081/swagger-ui/index.html

## Permisos y coordenadas

**Permisos (ASR-05):** toda escritura exige un token (`Authorization: Bearer`) que se verifica con la clave pública de autorización, y registro pregunta a `servicio-autorizacion` (`POST /verificar`, con caché de 60 s) si ese usuario tiene el permiso. Con un token falso o vencido responde 401; sin token o sin permiso, 403; si autorización no responde, 503 (no se escribe nada).

| Operación | Permiso |
|---|---|
| `POST /hallazgos`, `POST /avistamientos`, `POST /especies` | `hallazgo:crear` |
| `PATCH /hallazgos/{id}`, `POST /hallazgos/{id}/evidencias`, `POST /hallazgos/{id}/reclasificacion` | `hallazgo:editar` (y el `autorizador` de la reclasificación debe tener `hallazgo:validar`) |
| `POST /equipos`, `PUT /equipos/{id}/responsable`, `POST /expediciones`, `POST /expediciones/{id}/equipos` | `equipo:gestionar` |

El investigador o autor que se guarda es **el del token**: los campos `investigadorId` y `autor` del cuerpo se ignoran, para que nadie pueda registrar a nombre de otro. La cabecera `X-Usuario-Id` que mande el cliente también se ignora.

Las lecturas (`GET`) no exigen permiso, pero las coordenadas de hallazgos y avistamientos solo se entregan a quien tiene `ubicacion:ver_exacta` o acceso a esa especie. A los demás les llega el departamento y la región.

## Eventos

**Eventos que publica:** se guardan en la tabla `outbox_evento` en la misma transaccion del cambio y `RetransmisorOutbox` los envia cada 5 s a RabbitMQ.

- Exchange: `biohub.eventos` (tipo `topic`, durable)
- Routing key = tipo del evento: `HALLAZGO_REGISTRADO`, `HALLAZGO_MODIFICADO`, `AVISTAMIENTO_REGISTRADO`, `EQUIPO_ASIGNADO`
- Cuerpo JSON (`EventoDominio`): `idEvento`, `tipo`, `version`, `idAgregado`, `ocurrioEn`, `autor`, `autorizador`, `datos`

Cada servicio consumidor declara su propia cola y la enlaza al exchange con las routing keys que necesite. Hoy nadie consume `EQUIPO_ASIGNADO`.

**Eventos que consume:** ninguno.

## Como ejecutarlo

**Local (desarrollo):** con MySQL y RabbitMQ arriba (`docker compose up -d mysql rabbitmq` desde `infra/`), ejecutar `.\mvnw.cmd spring-boot:run` en esta carpeta.

**Docker:** desde `infra/`, `docker compose up -d --build servicio-registro`. Queda en http://localhost:8081.

Variables de entorno (por defecto apuntan a `localhost`): `PUERTO_SERVIDOR`, `BD_HOST`, `BD_PUERTO`, `BD_NOMBRE`, `BD_USUARIO`, `BD_CLAVE`, `RABBIT_HOST`, `RABBIT_PUERTO`, `RABBIT_USUARIO`, `RABBIT_CLAVE`, `AUTORIZACION_URL` (`http://localhost:8082`).
