# servicio-taxonomia

Fichas taxonómicas flexibles: cada especie tiene su propio conjunto de atributos (texto, numérico o lista de valores) tomados de un catálogo que se puede ampliar sin rediseñar nada. Base documental (MongoDB). Publica eventos con patrón Outbox.

**ASR:** ASR-08
**Historias:** HU-02.1 (crear fichas), HU-02.2 (editar fichas), HU-02.3 (desactivar conservando historial), HU-21.1 (agregar atributos nuevos), HU-21.2 (incorporar atributos a fichas existentes), HU-21.3 (atributos distintos por especie)
**Eventos que publica:** `FICHA_CREADA`, `FICHA_MODIFICADA`, `FICHA_DESACTIVADA` (exchange `biohub.eventos`, routing key = tipo)
**Eventos que consume:** ninguno

## Tecnología
Java 21 · Spring Boot 4.1 · Spring Data MongoDB (`taxonomia_db`) · Spring AMQP (RabbitMQ) · Puerto 8084

## Modelo
- **Catálogo de atributos** (colección `atributos`): `AtributoTaxonomico` con nombre único, `tipoDato` (`TEXTO`, `NUMERICO`, `LISTA`), unidad, opciones (solo `LISTA`) y si es obligatorio.
- **Fichas** (colección `fichas`): `FichaTaxonomica` de una especie con su versión, si está activa y su lista de atributos, cada uno con su valor (`ValorAtributo`: copia del atributo del catálogo + valor, que puede quedar vacío).
- El catálogo y las fichas empiezan vacíos: MongoDB no tiene datos iniciales.
- La especie vive en `servicio-registro`; la ficha guarda solo su `especieId` (los mismos UUID de `GET /especies` de registro). Una especie tiene como máximo una ficha activa.

## Patrones (dónde está cada uno)
| Patrón | Clase | Qué hace |
|---|---|---|
| Strategy | `TipoDato` | Cada tipo (`TEXTO`, `NUMERICO`, `LISTA`) tiene su propia regla `esValido` |
| Factory | `FabricaAtributo`, `FabricaEventos` | Crean atributos según su tipo y eventos con la misma estructura |
| Builder | `ConstructorFicha` | Arma la ficha agregando atributos uno a uno y valida todo en `build()` |
| Adapter | `PublicadorEventos` / `PublicadorRabbit` | El resto del código no depende de RabbitMQ |
| Outbox | `FichaServicio.guardar` + `RetransmisorEventos` | El evento se guarda dentro de la ficha y luego se publica |

Agregar un tipo de dato nuevo = una constante en `TipoDato` con su regla (y su caso en `FabricaAtributo` si necesita datos propios).

## API
| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/atributos` | Agrega un atributo al catálogo (HU-21.1) |
| GET | `/atributos` | Lista el catálogo |
| POST | `/fichas` | Crea la ficha de una especie con atributos del catálogo (HU-02.1, HU-21.3) |
| GET | `/fichas?especieId=&incluirInactivas=false` | Lista fichas; por defecto solo las activas |
| GET | `/fichas/{id}` | Consulta una ficha, también si está inactiva (HU-02.3) |
| PUT | `/fichas/{id}/atributos/{nombre}` | Cambia (o borra, si no es obligatorio) el valor de un atributo (HU-02.2) |
| POST | `/fichas/{id}/atributos` | Incorpora un atributo del catálogo a una ficha existente, con o sin valor (HU-21.2) |
| POST | `/fichas/{id}/desactivacion` | Desactiva la ficha sin borrarla (HU-02.3) |

Las lecturas son abiertas. Las escrituras exigen un token (`Authorization: Bearer`), que se verifica con la clave pública de autorización, y el permiso `ficha:editar`; la desactivación pide `ficha:desactivar`. El autor que se guarda es la cuenta del token: si el cuerpo trae `autor`, se ignora.

Swagger: http://localhost:8084/swagger-ui/index.html

Ejemplo para crear una ficha:
```json
{
  "especieId": "33333333-3333-3333-3333-333333333333",
  "atributos": [
    { "nombre": "temperatura del agua", "valor": "18.5" },
    { "nombre": "habitat" }
  ]
}
```

## Eventos
Cada cambio deja su evento dentro de la misma ficha (`eventosPendientes`), así la ficha y el evento se guardan en una sola escritura de MongoDB. `RetransmisorEventos` revisa cada 5 s, publica los pendientes en orden en RabbitMQ y los quita de la ficha; si RabbitMQ no responde, quedan pendientes para la siguiente vuelta. En casos raros un evento puede llegar dos veces: los consumidores pueden ignorar repetidos por `idEvento`.

Cuerpo JSON (`EventoDominio`, igual que en registro): `idEvento`, `tipo`, `version`, `idAgregado` (id de la ficha), `ocurrioEn`, `autor`, `autorizador`, `datos`.

- `FICHA_CREADA`: `especieId`, `version`, `atributos` (nombre → valor)
- `FICHA_MODIFICADA`: `especieId`, `campo`, `valorAnterior`, `valorNuevo`, `version` (al incorporar un atributo, `valorAnterior` es `null`)
- `FICHA_DESACTIVADA`: `especieId`, `motivo`, `version`

## Cómo ejecutarlo
**Local:** con MongoDB y RabbitMQ arriba (`docker compose up -d mongo rabbitmq` desde `infra/`), ejecutar `.\mvnw.cmd spring-boot:run` en esta carpeta.

**Docker:** desde `infra/`, `docker compose up -d --build servicio-taxonomia`.

Variables de entorno (por defecto apuntan a `localhost`): `PUERTO_SERVIDOR`, `MONGO_HOST`, `MONGO_PUERTO`, `MONGO_BD`, `RABBIT_HOST`, `RABBIT_PUERTO`, `RABBIT_USUARIO`, `RABBIT_CLAVE`, `AUTORIZACION_URL` (`http://localhost:8082`).
