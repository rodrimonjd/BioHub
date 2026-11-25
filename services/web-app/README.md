# BioHub — Sitio Web Oficial (`services/web-app`)

Interfaz web de **BioHub — Sistema Nacional de Registro y Vigilancia de Biodiversidad** construida con **Next.js 15 (App Router) + React 19 + TypeScript + Tailwind CSS v4** bajo la dirección estética **Editorial Naturalista**.

## Estructura de Rutas

- **`/` (`app/page.tsx`)**: Página pública institucional con el Hero topográfico, los 4 retos científicos del Instituto y acceso para iniciar sesión (inicia siempre sin sesión activa). El botón **"Login como invitado"** entra al panel sin cuenta: pide a `servicio-autorizacion` los permisos del id reservado `invitado` (rol CIUDADANO, solo `hallazgo:ver`), así que solo puede consultar.
- **`/login` (`app/login/page.tsx`)**: Inicio de sesión con usuario (o correo) y contraseña contra `servicio-autorizacion` (`POST /sesiones`). En el navegador quedan el nombre, los **roles y los permisos reales** de la cuenta (nunca la contraseña ni el token) y el panel los refresca al entrar.
- **`/panel` (`app/panel/page.tsx`)**: Panel científico protegido por sesión que integra:
  - **Estación de campo** (`FieldStationWorkspace.tsx`), con cuatro pestañas:
    - *Avistamientos*: registro diario. Si el servidor no responde, queda *"Guardado solo en este equipo, sin sincronizar"* con un botón para reintentar.
    - *Hallazgos*: registro con evidencias, y la consulta por especie, fecha y palabra clave (`ListaHallazgos.tsx`, HU-05), que lee de consulta pública.
    - *Reclasificación*: actualizar datos o cambiar la especie de un hallazgo, con confirmación.
    - *Equipos y expediciones* (`GestionCampo.tsx`): solo con `equipo:gestionar`. `ExpedicionesConEquipos.tsx` muestra los equipos de cada expedición (HU-04).
  - **Usuarios y roles** (`AdminUsuarios.tsx`, `CambioDeRol.tsx`): crear cuentas y cambiar su rol; la interfaz maneja un rol por cuenta. Solo con `roles:administrar`.
  - **Inventario y fichas taxonómicas** (`SpeciesCard.tsx`, `NuevaFicha.tsx`): buscador, atributos de cada ficha y formularios para crearlas y ampliarlas.
  - **Trazabilidad** (`HistorialRegistros.tsx`): cronología inmutable de hallazgos y fichas, leída de historial.
  - **Corredor biológico** (`UbicacionPorEspecie.tsx`): ubicación del hallazgo más reciente de una especie, leída de consulta pública. Las coordenadas exactas solo llegan con `ubicacion:ver_exacta` o con acceso a esa especie.
  - **Protección de ubicaciones** (`AccesoUbicaciones.tsx`): dar o quitar a una cuenta el acceso a una especie, con su historial. Solo con `ubicacion:administrar`.
  - **Plataforma abierta** (`ApiUsagePanel.tsx`): consumo por minuto y límite de la API externa.
  - **Reportes** (`ReportesPanel.tsx`): solicitarlos, ver su estado y exportarlos en PDF.

## Cómo ejecutar el proyecto

```bash
cd services/web-app
npm install
npm run dev
```

Abre `http://localhost:3000`. Si el contenedor `biohub-web-app` ya usa ese puerto, detenlo o usa otro: `npm run dev -- -p 3001`.

En Docker, las URLs de los servicios se fijan al compilar la imagen (`ARG` del `Dockerfile`), así que un cambio en ellas o en el código pide reconstruirla: `docker compose -f infra/docker-compose.yml up -d --build web-app`.

### Conexión con los servicios
El navegador llama a rutas del mismo origen y Next las reenvía (`next.config.ts`), así no hace falta CORS:

| Ruta del front | Servicio | Variable de entorno (por defecto) |
|---|---|---|
| `/api/registro/...` | servicio-registro | `REGISTRO_URL` (`http://localhost:8081`) |
| `/api/autorizacion/...` | servicio-autorizacion (`/api/v1/autorizacion/...`) | `AUTORIZACION_URL` (`http://localhost:8082`) |
| `/api/consulta/...` | servicio-consulta-publica (`/api/v1/consulta/...`) | `CONSULTA_URL` (`http://localhost:8083`) |
| `/api/taxonomia/...` | servicio-taxonomia | `TAXONOMIA_URL` (`http://localhost:8084`) |
| `/api/reportes/...` | servicio-reportes (`/reportes/...`) | `REPORTES_URL` (`http://localhost:8085`) |
| `/api/historial/...` | servicio-historial (`/api/v1/historial/...`) | `HISTORIAL_URL` (`http://localhost:8086`) |
| `/api/gateway/consumo`, `/api/gateway/limite` | api-gateway (`/gateway/...`) | `GATEWAY_URL` (`http://localhost:8080`) |
| `/api/externa/...` | api-gateway (`/api/v1/...`), para probar la API externa con una API key | `GATEWAY_URL` |
| `/api/sesion` | La propia web: inicia y cierra la sesión | `AUTORIZACION_URL` |

Para entrar al panel hace falta `servicio-autorizacion` en marcha (`docker compose -f infra/docker-compose.yml up -d`). Las cuentas de desarrollo están en el README de `servicio-autorizacion`.

### Permisos en la interfaz
`lib/permissions.ts` no decide quién puede qué: solo consulta los permisos que devolvió el backend.

| Acción | Permiso |
|---|---|
| Registrar avistamientos, hallazgos y especies | `hallazgo:crear` |
| Agregar evidencias, modificar y reclasificar hallazgos | `hallazgo:editar` |
| Autorizar reclasificaciones (y aparecer como autorizador) | `hallazgo:validar` |
| Ver coordenadas exactas | `ubicacion:ver_exacta`, o acceso a la especie |
| Dar o quitar acceso a la ubicación de una especie | `ubicacion:administrar` |
| Registrar equipos y expediciones | `equipo:gestionar` |
| Crear y editar fichas y atributos | `ficha:editar` |
| Desactivar fichas | `ficha:desactivar` |
| Ver el historial | `historial:ver` |
| Ver reportes | `reporte:ver` |
| Solicitar reportes científicos | `reporte:generar` |
| Ver el consumo de la API externa | `api_externa:monitorear` |
| Cambiar el límite de la API externa | `api_externa:configurar` |
| Administrar cuentas y roles | `roles:administrar` |

Esto solo muestra u oculta botones: la protección real está en el backend. Al iniciar sesión, `app/api/sesion` guarda el token en una cookie `HttpOnly` que el JavaScript no puede leer, y `middleware.ts` lo agrega a cada petición como `Authorization: Bearer`; cada servicio lo verifica y valida el permiso con autorización. Si el token vence, la web lleva al login. Si responde 403 o 400, el panel muestra el motivo; solo si el servidor no responde guarda el registro en el equipo para reintentarlo.

### Verificación de calidad

- **Linter (ESLint):** `npm run lint`
- **Compilación de producción:** `npm run build`
- **Pruebas E2E (Playwright a 1440 px y 375 px):** `npm run test:e2e`, con los servicios encendidos. Una de ellas registra un avistamiento en la base de datos.
