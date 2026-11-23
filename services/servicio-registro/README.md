# servicio-registro

Hallazgos, fichas taxonómicas flexibles, evidencias, equipos y avistamientos. BD documental (MongoDB). Publica eventos con patrón Outbox.

**ASR:** ASR-05, ASR-06, ASR-08, ASR-09

**Permisos (ASR-05):** toda escritura exige la cabecera `X-Usuario-Id` y registro pregunta a `servicio-autorizacion` (`POST /verificar`, con caché de 60 s) si ese usuario tiene el permiso. Sin cabecera o sin permiso responde 403; si autorización no responde, 503 (no se escribe nada).

| Operación | Permiso |
|---|---|
| `POST /hallazgos`, `POST /avistamientos` | `hallazgo:crear` |
| `PATCH /hallazgos/{id}`, `POST /hallazgos/{id}/reclasificacion` | `hallazgo:editar` (y el `autorizador` de la reclasificación debe tener `hallazgo:validar`) |
| `POST /equipos`, `PUT /equipos/{id}/responsable`, `POST /expediciones`, `POST /expediciones/{id}/equipos` | `equipo:gestionar` |

El investigador o autor que se guarda es **el de la cabecera**: los campos `investigadorId` y `autor` del cuerpo se ignoran, para que nadie pueda registrar a nombre de otro. Las lecturas (`GET`) no exigen permiso. Variable de entorno: `AUTORIZACION_URL` (`http://localhost:8082`).

**Eventos que publica:** se guardan en la tabla `outbox_evento` en la misma transaccion del cambio y `RetransmisorOutbox` los envia cada 5 s a RabbitMQ.

- Exchange: `biohub.eventos` (tipo `topic`, durable)
- Routing key = tipo del evento: `HALLAZGO_REGISTRADO`, `HALLAZGO_MODIFICADO`, `AVISTAMIENTO_REGISTRADO`, `EQUIPO_ASIGNADO`
- Cuerpo JSON (`EventoDominio`): `idEvento`, `tipo`, `version`, `idAgregado`, `ocurrioEn`, `autor`, `autorizador`, `datos`

Cada servicio consumidor declara su propia cola y la enlaza al exchange con las routing keys que necesite (por ejemplo `HALLAZGO_*`).

**Eventos que consume:** ninguno.

## Como ejecutarlo

**Local (desarrollo):** con MySQL y RabbitMQ arriba (`docker compose up -d mysql rabbitmq` desde `infra/`), ejecutar `.\mvnw.cmd spring-boot:run` en esta carpeta.

**Docker:** desde `infra/`, `docker compose up -d --build servicio-registro`. Queda en http://localhost:8081 (Swagger en `/swagger-ui/index.html`).

Variables de entorno (por defecto apuntan a `localhost`): `PUERTO_SERVIDOR`, `BD_HOST`, `BD_PUERTO`, `BD_NOMBRE`, `BD_USUARIO`, `BD_CLAVE`, `RABBIT_HOST`, `RABBIT_PUERTO`, `RABBIT_USUARIO`, `RABBIT_CLAVE`.
