# Infraestructura

Message broker, bases de datos y docker-compose del entorno local.

## Levantar todo

Desde la raíz del repositorio, levanta bases de datos, RabbitMQ, los servicios, el API Gateway y el front:

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

El front queda en http://localhost:3000. Para trabajar en el front con recarga en vivo, detén su
contenedor (`docker compose -f infra/docker-compose.yml stop web-app`) y usa `npm run dev` en `services/web-app`.
