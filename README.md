# BioHub — Sistema Nacional de Registro y Vigilancia de Biodiversidad

Proyecto semestral de **Ingeniería de Software 2** · Universidad El Bosque.
Plataforma que centraliza, protege y divulga el inventario nacional de flora y fauna.

## Equipo
Juan David Rodriguez · Raul Sebastian Escobar · Sebastian Garcia · Sebastian Bonilla · Julian Rodriguez

## Arquitectura
Microservicios + arquitectura orientada a eventos (EDA) + API Gateway. Cada servicio con su propia base de datos.

- **Web:** el proxy de Next.js llama directo a cada servicio con el token de la sesión.
- **Externos** (universidades y ONG): entran por el API Gateway con su API key, hasta 60 solicitudes por minuto.
- **Identidad y permisos:** autorización firma el token de la sesión y cada servicio lo verifica con su clave pública (HU-8.2).
- **Eventos:** registro y taxonomía publican en RabbitMQ; historial, reportes y consulta pública consumen.

| Contenedor | Carpeta | Puerto | Datos |
|---|---|---|---|
| Aplicación web (Next.js) | [`services/web-app`](services/web-app/README.md) | 3000 | — |
| API Gateway (autenticación, 60 solicitudes por minuto, enrutamiento) | [`services/api-gateway`](services/api-gateway/README.md) | 8080 | — |
| Servicio de registro (especies, hallazgos, evidencias, expediciones, equipos de campo, avistamientos) | [`services/servicio-registro`](services/servicio-registro/README.md) | 8081 | MySQL (`registro_db`) |
| Servicio de autorización (cuentas, sesiones, roles, permisos y acceso a ubicaciones) | [`services/servicio-autorizacion`](services/servicio-autorizacion/README.md) | 8082 | MySQL (`auth_db`) |
| Servicio de consulta pública (modelo de lectura) | [`services/servicio-consulta-publica`](services/servicio-consulta-publica/README.md) | 8083 | MySQL (`consulta_db`, alimentada por eventos) |
| Servicio de taxonomía (fichas taxonómicas flexibles y sus atributos) | [`services/servicio-taxonomia`](services/servicio-taxonomia/README.md) | 8084 | MongoDB (`taxonomia_db`) |
| Servicio de reportes (índices de presión, segundo plano) | [`services/servicio-reportes`](services/servicio-reportes/README.md) | 8085 | MySQL (`reportes_db`) |
| Servicio de historial (versiones inmutables) | [`services/servicio-historial`](services/servicio-historial/README.md) | 8086 | MySQL (`historial_db`, solo inserción) |
| Message broker (RabbitMQ, AMQP) y bases de datos | [`infra`](infra/README.md) | 5672 · 3306 · 27017 | — |

Patrones: Singleton, Factory, Builder, Adapter, Strategy, Observer y Outbox. El Decorator propuesto en la Entrega 2 no se implementó.

## Cómo ejecutarlo
Con Docker, desde la raíz:

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

- Web: http://localhost:3000 (también se puede entrar como invitado, sin cuenta).
- API externa: http://localhost:8080/api/v1/inventario, con la cabecera `X-API-Key`.
- Cuentas de desarrollo: [README de autorización](services/servicio-autorizacion/README.md#cuentas-e-inicio-de-sesión). API keys de demostración: [README del gateway](services/api-gateway/README.md#consumidores-de-demostración).
- Para empezar con las bases vacías: [README de infra](infra/README.md#empezar-con-las-bases-vacías).

## Pruebas
| Qué | Comando |
|---|---|
| Un servicio | `mvn test` en su carpeta (una prueba de registro y una de taxonomía arrancan el servicio completo: con las bases de datos encendidas) |
| Web, de extremo a extremo | `npm run test:e2e` en `services/web-app`, con el entorno encendido |
| Rendimiento de la consulta pública (HU-14.1) | `node tests/rendimiento/consulta-publica.mjs`, con el entorno encendido y hallazgos registrados |

## Documentación
- Documento de la entrega: `docs/entrega-2-arquitectura/Entrega_2_proyecto_biohub.docx`
- Modelo C4: `docs/entrega-2-arquitectura/c4/`
- UML (despliegue, componentes, clases, 8 secuencias): `docs/entrega-2-arquitectura/uml/`
- Resultados de rendimiento (HU-14.1): [docs/entrega-3-implementacion/rendimiento](docs/entrega-3-implementacion/rendimiento/resultados-HU-14.1.md)
- Historias comprometidas y reparto del equipo (18 historias, 93 puntos): [docs/backlog.md](docs/backlog.md)

## Entregas
| Entrega | Carpeta |
|---|---|
| 1 — SRS | `docs/entrega-1-srs/` |
| 2 — Arquitectura y diseño | `docs/entrega-2-arquitectura/` |
| 3 — Implementación y cierre | `services/`, `tests/`, `docs/entrega-3-implementacion/` |

Antes de contribuir, lee [CONTRIBUTING.md](CONTRIBUTING.md).
