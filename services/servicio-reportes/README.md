# servicio-reportes

Reportes científicos con índices de presión sobre los ecosistemas. Se calculan en segundo plano a partir de los avistamientos que publica `servicio-registro`, así el análisis nunca bloquea ni se cae con el registro diario.

**ASR:** ASR-09
**Historias:** HU-12.1 (configurar parámetros), HU-12.2 (generar reportes), HU-12.3 (exportar), HU-22.x (registro independiente de los reportes)
**Eventos que consume:** `AVISTAMIENTO_REGISTRADO` (de `servicio-registro`, exchange `biohub.eventos`)
**Eventos que publica:** ninguno

## Tecnología
Java 21 · Spring Boot 4.1 · Spring Data JPA · MySQL 8 (`reportes_db`) · Flyway · Spring AMQP (RabbitMQ) · OpenPDF · Puerto 8085

## Cómo funciona
```
servicio-registro ──(AVISTAMIENTO_REGISTRADO)──► cola reportes.avistamientos ──► ConsumidorAvistamientos
                                                       │ (1 intento + 2 reintentos con espera creciente; luego reportes.avistamientos.dlq)
                                                       ▼
                                             reportes_db.avistamiento   (copia propia: "BD analítica")

POST /reportes ──► Reporte EN_COLA ──► GeneradorReportes (cada 3 s, en segundo plano)
                                          GENERANDO → todos los índices por ecosistema + resumen por especie → LISTO / SIN_DATOS
```
- Reportes guarda su propia copia de los avistamientos: nunca consulta la base de registro. Si reportes está apagado, los avistamientos esperan en la cola durable.
- Si un evento llega repetido (mismo id de avistamiento) se ignora.
- **Ecosistema** = `departamento - región` del avistamiento (o solo el departamento si no tiene región).

## Índices (patrón Strategy, `TipoIndice`)
| Tipo | Cálculo por ecosistema | Umbral por defecto |
|---|---|---|
| `DENSIDAD_OBSERVACION` | individuos observados / días del periodo | 10 |
| `RIQUEZA_ESPECIES` | número de especies distintas observadas | 5 |

Cada reporte calcula **todos** los índices para cada ecosistema y marca los que superan su umbral. Agregar un índice nuevo = una constante en `TipoIndice` con su cálculo; aparece solo en el JSON y en el PDF.

## API (`/reportes`)
| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/reportes` | Solicita un reporte; responde **202** con el reporte `EN_COLA` |
| GET | `/reportes?solicitadoPor=` | Lista reportes (más recientes primero) |
| GET | `/reportes/{id}` | Estado del reporte, sus índices y el resumen por especie |
| GET | `/reportes/{id}/exportar?formato=pdf\|json` | Informe en PDF (por defecto) o JSON. Solo si está `LISTO`; si no, 409 |

Estados: `EN_COLA` → `GENERANDO` → `LISTO`, o `SIN_DATOS` si no hay avistamientos para los parámetros, o `ERROR`.

Ejemplo de solicitud:
```json
{
  "fechaInicio": "2026-11-01",
  "fechaFin": "2026-11-30",
  "departamento": "Meta",
  "especieId": null,
  "solicitadoPor": "director-1"
}
```
`departamento` y `especieId` son opcionales. Las fechas se interpretan en hora de Colombia.

## Informe PDF (`DocumentoPdfReporte`)
Encabezado institucional, datos de identificación (código, solicitante, fechas, periodo, filtros) y secciones:
1. Objetivo · 2. Resumen general · 3. Índices de presión por ecosistema (tabla, `*` = supera el umbral) · 4. Avistamientos por especie · 5. Metodología · 6. Conclusiones (ecosistemas en alerta y especie con más individuos). Pie de página con el código del reporte y el número de página.

Swagger: http://localhost:8085/swagger-ui/index.html

## Cómo ejecutarlo
**Docker:** desde `infra/`, `docker compose up -d --build servicio-reportes`.

**Local:** con MySQL y RabbitMQ arriba, `.\mvnw.cmd spring-boot:run` en esta carpeta. Variables: `PUERTO_SERVIDOR`, `BD_HOST`, `BD_PUERTO`, `BD_NOMBRE`, `BD_USUARIO`, `BD_CLAVE`, `RABBIT_HOST`, `RABBIT_PUERTO`, `RABBIT_USUARIO`, `RABBIT_CLAVE`.

> Los avistamientos registrados antes de que exista la cola de reportes no llegan a este servicio (RabbitMQ solo entrega los eventos nuevos).
