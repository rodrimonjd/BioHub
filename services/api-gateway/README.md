# api-gateway

Punto de entrada único para consumidores externos (universidades y ONG): autentica la API key, aplica el límite de 60 solicitudes por minuto por consumidor (HTTP 429), consulta al servicio de autorización y enruta hacia los servicios internos con una URL estable (RNF-11).

**ASR:** ASR-03
**Historias:** HU-11.1 (autenticar consumidores), HU-11.2 (consultar mediante acceso externo), HU-11.3 (formato estructurado), HU-16 (control de solicitudes por consumidor)
**Eventos que publica / consume:** ninguno

## Tecnología
Java 21 · Spring Boot 4.1 · Spring Web MVC (`RestClient`) · Puerto 8080 · Sin base de datos

## Cómo funciona (secuencia 07)
```
Consumidor ── GET /api/v1/inventario + X-API-Key ──► ApiGateway
   1. ConsumidorExterno.autenticar(apiKey)          → 401 si la key falta o no es válida
   2. ConsumidorExterno.registrarSolicitud()        → 429 + Retry-After si pasa de 60 en el minuto
   3. servicio-autorizacion: validarConsumidor(id)   → 403 si no tiene permiso, 503 si no responde
   4. Ruta /api/v1/inventario → servicio-consulta-publica /api/v1/consulta/hallazgos
      (con la cabecera X-Usuario-Id = id del consumidor)       → 200 + JSON
```
- El límite es por consumidor y por minuto del reloj: al empezar el minuto siguiente el contador vuelve a 0.
- Consulta pública decide qué tan exacta es la ubicación según los permisos del consumidor (por defecto, región o departamento).

## Clases
| Clase | Qué hace |
|---|---|
| `ApiGateway` | `enrutar(ruta, credencial)` aplica los 4 pasos; `actualizarRuta(ruta, destino)` cambia el servicio interno de una ruta pública |
| `ConsumidorExterno` | `autenticar(apiKey)` compara el SHA-256 de la key; `registrarSolicitud()` cuenta las solicitudes del minuto |
| `GatewayProperties` | Configuración única (Singleton): límite por minuto, rutas y consumidores |
| `ClienteAutorizacion` | Llama a `POST /api/v1/autorizacion/consumidores/validar` |
| `GatewayController` | Recibe `GET /api/v1/**` y se lo pasa a `ApiGateway` |

## API pública
| Método | Ruta | Servicio interno |
|---|---|---|
| GET | `/api/v1/inventario` | `GET /api/v1/consulta/hallazgos` (filtros: `especieId`, `departamento`, `especie`, `pagina`, `tamano`) |
| GET | `/api/v1/inventario/{id}` | `GET /api/v1/consulta/hallazgos/{id}` |

Errores en JSON: `{"estado": 429, "error": "Too Many Requests", "mensaje": "..."}`.

## Consumidores de demostración
Se configuran en `application.yml` (`biohub.gateway.consumidores`) con el SHA-256 de su key; los permisos viven en `servicio-autorizacion` (migración `V3__consumidores_externos.sql`, rol `CONSUMIDOR_EXTERNO`).

| Consumidor | API key (demo) | Resultado |
|---|---|---|
| `consumidor-unal` | `unal-demo-2026` | 200 |
| `consumidor-humboldt` | `humboldt-demo-2026` | 200 |
| `consumidor-ong-suspendida` | `ong-suspendida-demo-2026` | 403 (no tiene el rol en autorización) |

Ejemplo:
```powershell
curl.exe -H "X-API-Key: unal-demo-2026" "http://localhost:8080/api/v1/inventario?departamento=Meta"
```

Para agregar un consumidor: calcular el SHA-256 de su key, agregarlo en `application.yml` y darle el rol `CONSUMIDOR_EXTERNO` en autorización.

## Cómo ejecutarlo
**Docker:** desde `infra/`, `docker compose up -d --build api-gateway` (necesita autorización y consulta pública).

**Local:** `.\mvnw.cmd spring-boot:run` en esta carpeta. Variables: `PUERTO_SERVIDOR`, `URL_PUBLICA`, `AUTORIZACION_URL` (por defecto `http://localhost:8082`), `CONSULTA_URL` (por defecto `http://localhost:8083`).
