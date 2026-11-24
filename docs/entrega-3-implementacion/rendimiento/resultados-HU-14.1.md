# Pruebas de rendimiento — HU-14.1

**Historia:** como visitante o consumidor del sistema, quiero que las consultas públicas respondan en menos de un segundo.

**Resultado: CUMPLE** — criterio: sin errores y percentil 95 por debajo de 1000 ms en cada escenario.

- Fecha de ejecución: 2026-11-24T04:03:49.550Z
- Solicitudes medidas por escenario: 500 (25 en paralelo), después de 20 de calentamiento que no se cuentan
- Por el API Gateway: 50 solicitudes, por debajo del límite de 60 por minuto (ASR-03)
- Equipo: 16 núcleos (13th Gen Intel(R) Core(TM) i7-13620H), 7.6 GB de RAM, Node v22.23.5
- Servicio medido: `http://host.docker.internal:8083` · Gateway: `http://host.docker.internal:8080`

| Escenario | Solicitudes | Errores | p50 | p95 | p99 | Máximo | Bajo 1000 ms | Solicitudes/s | Cumple |
|---|---|---|---|---|---|---|---|---|---|
| Listado público | 500 | 0 | 32.0 ms | 56.6 ms | 66.3 ms | 86.3 ms | 100.0 % | 718 | Sí |
| Filtro por departamento | 500 | 0 | 17.0 ms | 25.9 ms | 29.1 ms | 43.1 ms | 100.0 % | 1416 | Sí |
| Búsqueda por especie | 500 | 0 | 14.6 ms | 29.1 ms | 38.8 ms | 42.3 ms | 100.0 % | 1523 | Sí |
| Rango de fechas | 500 | 0 | 19.8 ms | 28.5 ms | 34.6 ms | 40.9 ms | 100.0 % | 1223 | Sí |
| Detalle de un hallazgo | 500 | 0 | 17.2 ms | 31.4 ms | 38.1 ms | 44.5 ms | 100.0 % | 1361 | Sí |
| API Gateway (consumidor externo) | 50 | 0 | 34.7 ms | 234.3 ms | 241.7 ms | 241.7 ms | 100.0 % | 90 | Sí |

## Escenarios
- **Listado público**: Primera página del inventario público (20 hallazgos).
- **Filtro por departamento**: Hallazgos del departamento Vichada.
- **Búsqueda por especie**: Búsqueda de texto por la especie "Crocodylus intermedius".
- **Rango de fechas**: Hallazgos registrados entre 2026-01-01 y 2026-11-24.
- **Detalle de un hallazgo**: Detalle por id, rotando entre 5 hallazgos.
- **API Gateway (consumidor externo)**: Inventario por la URL pública estable, con la API key de un consumidor y por debajo de su límite por minuto.

## Cómo repetir la prueba
Con el entorno levantado (`docker compose -f infra/docker-compose.yml up -d`), desde la raíz del repositorio:

```bash
node tests/rendimiento/consulta-publica.mjs
```

El script termina con código 1 si algún escenario no cumple, para poder usarlo en integración continua.
