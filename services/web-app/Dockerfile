# Etapa de dependencias
FROM node:22-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

# Etapa de compilacion
FROM node:22-alpine AS build
WORKDIR /app
# Next guarda los destinos de los rewrites al compilar, asi que las URLs de los servicios se pasan aqui.
# Por defecto, los nombres de los servicios dentro de la red de docker compose.
ARG REGISTRO_URL=http://servicio-registro:8081
ARG AUTORIZACION_URL=http://servicio-autorizacion:8082
ARG CONSULTA_URL=http://servicio-consulta-publica:8083
ARG TAXONOMIA_URL=http://servicio-taxonomia:8084
ARG REPORTES_URL=http://servicio-reportes:8085
ARG HISTORIAL_URL=http://servicio-historial:8086
ARG GATEWAY_URL=http://api-gateway:8080
ENV REGISTRO_URL=$REGISTRO_URL \
    AUTORIZACION_URL=$AUTORIZACION_URL \
    CONSULTA_URL=$CONSULTA_URL \
    TAXONOMIA_URL=$TAXONOMIA_URL \
    REPORTES_URL=$REPORTES_URL \
    HISTORIAL_URL=$HISTORIAL_URL \
    GATEWAY_URL=$GATEWAY_URL \
    NEXT_TELEMETRY_DISABLED=1
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

# Etapa de ejecucion: solo el servidor standalone de Next
FROM node:22-alpine
WORKDIR /app
# La usa /api/sesion para iniciar sesion (se lee al ejecutar, no al compilar)
ARG AUTORIZACION_URL=http://servicio-autorizacion:8082
ENV AUTORIZACION_URL=$AUTORIZACION_URL \
    NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1 \
    PORT=3000 \
    HOSTNAME=0.0.0.0
RUN addgroup -S biohub && adduser -S biohub -G biohub
COPY --from=build --chown=biohub:biohub /app/.next/standalone ./
COPY --from=build --chown=biohub:biohub /app/.next/static ./.next/static
USER biohub
EXPOSE 3000
CMD ["node", "server.js"]
