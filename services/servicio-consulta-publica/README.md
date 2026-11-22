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
GET /api/v1/consulta/hallazgos ──► ServicioConsulta ──┬── CatalogoEspecies (GET /especies de registro, caché 5 min)
                                                      └── ClienteAutorizacion (POST /visibilidad-ubicacion, caché 60 s)
```
- La cola es durable: si el servicio está apagado, los eventos esperan en RabbitMQ y se aplican al arrancar.
- El proyector es idempotente: un evento con versión menor o igual a la guardada se ignora.
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

## API (`/api/v1/consulta`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/hallazgos` | Listado paginado. Filtros opcionales: `especieId`, `departamento`, `especie` (texto en el nombre científico), `pagina` (0), `tamano` (20, máx. 100) |
| GET | `/hallazgos/{id}` | Un hallazgo |

Cabecera opcional `X-Usuario-Id` (la pone el API Gateway). Sin ella la consulta es anónima.

### Visibilidad de la ubicación (`nivelUbicacion`)
| Nivel | Coordenadas | Región | Departamento |
|---|---|---|---|
| `EXACTA` | ✅ | ✅ | ✅ |
| `REGION` | — | ✅ | ✅ |
| `DEPARTAMENTO` | — | — | ✅ |

Con las reglas actuales de `servicio-autorizacion`: el público ve exactas las especies LC/NT, región las VU y solo departamento las CR/EN; quien tiene `ubicacion:ver_exacta` (ADMIN, AUTORIDAD_AMBIENTAL) siempre ve coordenadas.

## Ejemplos (PowerShell)
```powershell
curl.exe "http://localhost:8083/api/v1/consulta/hallazgos?departamento=Meta"

curl.exe http://localhost:8083/api/v1/consulta/hallazgos/<id> -H "X-Usuario-Id: admin"
```

## Patrones
- **Observer:** `ConsumidorEventos` se suscribe a los eventos de registro.
- **Adapter:** `CatalogoEspecies` y `ClienteAutorizacion` aíslan las llamadas HTTP a otros servicios.
- **Modelo de lectura (CQRS):** `hallazgo_lectura` es una copia optimizada para consultas.
