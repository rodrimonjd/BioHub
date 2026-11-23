# BioHub — Sitio Web Oficial (`services/web-app`)

Interfaz web de **BioHub — Sistema Nacional de Registro y Vigilancia de Biodiversidad** construida con **Next.js 15 (App Router) + React 19 + TypeScript + Tailwind CSS v4** bajo la dirección estética **Editorial Naturalista**.

## Estructura de Rutas

- **`/` (`app/page.tsx`)**: Página pública institucional con el Hero topográfico, los 4 retos científicos del Instituto y acceso para iniciar sesión (inicia siempre sin sesión activa). El botón **"Login como invitado"** entra al panel sin cuenta: pide a `servicio-autorizacion` los permisos del id reservado `invitado` (rol CIUDADANO, solo `hallazgo:ver`), así que solo puede consultar.
- **`/login` (`app/login/page.tsx`)**: Inicio de sesión con usuario (o correo) y contraseña contra `servicio-autorizacion` (`POST /sesiones`). La sesión guarda los **roles y permisos reales** del usuario (nunca la contraseña) y el panel los refresca al entrar.
- **`/panel` (`app/panel/page.tsx`)**: Panel científico protegido por sesión que integra:
  - **Estación de campo (`components/biohub/FieldStationWorkspace.tsx`)**: Registro de avistamientos diarios (con estado explícito *"Guardado solo en este equipo, sin sincronizar"* y botón de reintento cuando el backend no está disponible, más paginación incremental), registro de hallazgos con evidencias y reclasificación con diálogo nativo `<dialog>` de confirmación.
  - **Inventario y fichas taxonómicas (`components/biohub/SpeciesCard.tsx`)**: Tarjetas compactas con botón desplegable *"Ver atributos (n)"*, buscador con estado vacío y formulario titulado *"Agregar atributo a una ficha"*.
  - **Trazabilidad y corredor biológico (`components/biohub/HistoryTimeline.tsx`, `components/biohub/ProtectedLocation.tsx`)**: Línea de tiempo inmutable y mapa cartográfico de la cuenca del Meta con selector local *"Ver tarjeta como"* (*Visitante común* vs. *Mis permisos*). Las coordenadas exactas solo se muestran si la cuenta tiene el permiso `ubicacion:ver_exacta`.
  - **API y reportes (`components/biohub/ApiUsagePanel.tsx`, `components/biohub/ReportStatus.tsx`)**: Panel de consumo por minuto y estado de reportes en segundo plano.

## Cómo ejecutar el proyecto

```bash
cd services/web-app
npm install
npm run dev
```

Abre `http://localhost:3000`.

### Conexión con los servicios
El navegador llama a rutas del mismo origen y Next las reenvía (`next.config.ts`), así no hace falta CORS:

| Ruta del front | Servicio | Variable de entorno (por defecto) |
|---|---|---|
| `/api/registro/...` | servicio-registro | `REGISTRO_URL` (`http://localhost:8081`) |
| `/api/autorizacion/...` | servicio-autorizacion (`/api/v1/autorizacion/...`) | `AUTORIZACION_URL` (`http://localhost:8082`) |

Para entrar al panel hace falta `servicio-autorizacion` en marcha (`docker compose -f infra/docker-compose.yml up -d`). Las cuentas de desarrollo están en el README de `servicio-autorizacion`.

### Permisos en la interfaz
`lib/permissions.ts` no decide quién puede qué: solo consulta los permisos que devolvió el backend.

| Acción | Permiso |
|---|---|
| Registrar avistamientos y hallazgos | `hallazgo:crear` |
| Modificar y reclasificar hallazgos | `hallazgo:editar` |
| Autorizar reclasificaciones (y aparecer como autorizador) | `hallazgo:validar` |
| Ver coordenadas exactas de especies sensibles | `ubicacion:ver_exacta` |
| Solicitar reportes científicos | `reporte:generar` |

Esto solo muestra u oculta botones: la protección real está en el backend. Cada escritura a registro envía la cabecera `X-Usuario-Id` y registro valida el permiso con autorización. Si responde 403 o 400, el panel muestra el motivo; solo si el servidor no responde guarda el registro en el equipo para reintentarlo.

### Verificación de calidad

- **Linter (ESLint):** `npm run lint`
- **Compilación de producción:** `npm run build`
- **Pruebas E2E (Playwright a 1440 px y 375 px):** `npm run test:e2e`
