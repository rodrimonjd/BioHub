# Infraestructura

Message broker, bases de datos y docker-compose del entorno local.

## Levantar todo

Desde la raíz del repositorio, levanta bases de datos, RabbitMQ, los servicios, el API Gateway y el front:

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

El front queda en http://localhost:3000. Para trabajar en el front con recarga en vivo, detén su
contenedor (`docker compose -f infra/docker-compose.yml stop web-app`) y usa `npm run dev` en `services/web-app`.

| Contenedor | Puerto |
|---|---|
| `biohub-web-app` | 3000 |
| `biohub-api-gateway` | 8080 |
| `biohub-servicio-registro` | 8081 |
| `biohub-servicio-autorizacion` | 8082 |
| `biohub-servicio-consulta-publica` | 8083 |
| `biohub-servicio-taxonomia` | 8084 |
| `biohub-servicio-reportes` | 8085 |
| `biohub-servicio-historial` | 8086 |
| `biohub-mysql` | 3306 |
| `biohub-mongo` | 27017 |
| `biohub-rabbitmq` | 5672 (consola en http://localhost:15672, usuario y clave `biohub`) |

## Datos

- `mysql/init.sql` crea las cinco bases MySQL y sus usuarios. Solo corre con el volumen vacío.
- Flyway crea las tablas y los datos iniciales de cada servicio al arrancar. **No edites una migración que ya se aplicó**, ni siquiera sus comentarios: el servicio no arrancaría con las bases existentes. Agrega una nueva.
- MongoDB no tiene datos iniciales: las fichas y el catálogo de atributos empiezan vacíos.

| Volumen | Qué guarda |
|---|---|
| `biohub_mysql_data` | Las cinco bases MySQL |
| `biohub_mongo_data` | Fichas y atributos |
| `biohub_rabbitmq_data` | Colas y mensajes pendientes |
| `biohub_auth_claves` | Clave privada que firma las sesiones |

### Empezar con las bases vacías

Deja solo lo que crean las migraciones (tres especies, tres cuentas, cinco roles y dos consumidores externos).
**Borra todos los datos del equipo y no se puede deshacer.**

```bash
docker compose -f infra/docker-compose.yml down
docker volume rm biohub_mysql_data biohub_mongo_data biohub_rabbitmq_data
docker compose -f infra/docker-compose.yml up -d
```

No uses `down -v`: borra también la clave de las sesiones y todos tendrían que volver a entrar.

## Variables de entorno (opcionales)

| Variable | Por defecto | Para qué |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | `root` | Clave de root de MySQL |
| `RABBITMQ_USER`, `RABBITMQ_PASS` | `biohub` | Usuario y clave de RabbitMQ |
| `SECRETO_SERVICIOS` | `biohub-desarrollo` | Lo comparten el API Gateway y autorización para pedir tokens de consumidores externos. Cámbialo fuera de desarrollo |
