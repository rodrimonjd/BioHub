# servicio-registro

Hallazgos, fichas taxonómicas flexibles, evidencias, equipos y avistamientos. BD documental (MongoDB). Publica eventos con patrón Outbox.

**ASR:** ASR-06, ASR-08, ASR-09

**Eventos que publica:** se guardan en la tabla `outbox_evento` en la misma transaccion del cambio y `RelayOutbox` los envia cada 5 s a RabbitMQ.

- Exchange: `biohub.eventos` (tipo `topic`, durable)
- Routing key = tipo del evento: `HALLAZGO_REGISTRADO`, `HALLAZGO_MODIFICADO`, `AVISTAMIENTO_REGISTRADO`, `EQUIPO_ASIGNADO`
- Cuerpo JSON (`EventoDominio`): `idEvento`, `tipo`, `version`, `idAgregado`, `ocurrioEn`, `autor`, `autorizador`, `datos`

Cada servicio consumidor declara su propia cola y la enlaza al exchange con las routing keys que necesite (por ejemplo `HALLAZGO_*`).

**Eventos que consume:** ninguno.
