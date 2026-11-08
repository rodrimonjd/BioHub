# BioHub — Sistema Nacional de Registro y Vigilancia de Biodiversidad

Proyecto semestral de **Ingeniería de Software 2** · Universidad El Bosque.
Plataforma que centraliza, protege y divulga el inventario nacional de flora y fauna.

## Equipo
Juan David Rodriguez · Raul Sebastian Escobar · Sebastian Garcia · Julian Rodriguez · Sebastian Bonilla

## Arquitectura (Entrega 2)
Microservicios + arquitectura orientada a eventos (EDA) + API Gateway. Cada servicio con su propia base de datos.

| Contenedor | Carpeta | Datos |
|---|---|---|
| Aplicación web | `services/web-app` | — |
| API Gateway (autenticación, rate limiting 60 req/min, enrutamiento) | `services/api-gateway` | — |
| Servicio de autorización (roles y permisos centralizados) | `services/servicio-autorizacion` | — |
| Servicio de registro (hallazgos, fichas flexibles, evidencias, equipos, avistamientos) | `services/servicio-registro` | MongoDB |
| Servicio de consulta pública (modelo de lectura con caché) | `services/servicio-consulta-publica` | BD de lectura (réplica + caché) |
| Servicio de historial (versiones inmutables) | `services/servicio-historial` | Almacenamiento de eventos |
| Servicio de reportes (índices de presión, segundo plano) | `services/servicio-reportes` | BD analítica |
| Message broker | `infra/` | — |

Patrones: Singleton, Factory, Builder, Adapter, Decorator, Strategy, Observer, Outbox.

## Documentación
- Documento de la entrega: `docs/entrega-2-arquitectura/Entrega_2_proyecto_biohub.docx`
- Modelo C4: `docs/entrega-2-arquitectura/c4/`
- UML (despliegue, componentes, clases, 8 secuencias): `docs/entrega-2-arquitectura/uml/`
- Historias comprometidas (18 historias, 93 puntos): [docs/backlog.md](docs/backlog.md)

## Entregas
| Entrega | Carpeta |
|---|---|
| 1 — SRS | `docs/entrega-1-srs/` |
| 2 — Arquitectura y diseño | `docs/entrega-2-arquitectura/` |
| 3 — Implementación y cierre | `services/`, `tests/`, `docs/entrega-3-implementacion/` |

Antes de contribuir, lee [CONTRIBUTING.md](CONTRIBUTING.md).
