# BioHub — Sitio Web Oficial (`services/web-app`)

Interfaz web de **BioHub — Sistema Nacional de Registro y Vigilancia de Biodiversidad** construida con **Next.js 15 (App Router) + React 19 + TypeScript + Tailwind CSS v4** bajo la dirección estética **Editorial Naturalista**.

## Estructura de Rutas

- **`/` (`app/page.tsx`)**: Página pública institucional con el Hero topográfico, los 4 retos científicos del Instituto y acceso para iniciar sesión (inicia siempre sin sesión activa).
- **`/login` (`app/login/page.tsx`)**: Página de acceso institucional con etiqueta de *Modo demo*, selección de perfil por rol y persistencia de sesión en el navegador.
- **`/panel` (`app/panel/page.tsx`)**: Panel científico protegido por sesión que integra:
  - **Estación de campo (`components/biohub/FieldStationWorkspace.tsx`)**: Registro de avistamientos diarios (con estado explícito *"Guardado solo en este equipo, sin sincronizar"* y botón de reintento cuando el backend no está disponible, más paginación incremental), registro de hallazgos con evidencias y reclasificación con diálogo nativo `<dialog>` de confirmación.
  - **Inventario y fichas taxonómicas (`components/biohub/SpeciesCard.tsx`)**: Tarjetas compactas con botón desplegable *"Ver atributos (n)"*, buscador con estado vacío y formulario titulado *"Agregar atributo a una ficha"*.
  - **Trazabilidad y corredor biológico (`components/biohub/HistoryTimeline.tsx`, `components/biohub/ProtectedLocation.tsx`)**: Línea de tiempo inmutable y mapa cartográfico de la cuenca del Meta con selector local *"Ver tarjeta como"* (*Visitante común* vs. *Alto rango*).
  - **API y reportes (`components/biohub/ApiUsagePanel.tsx`, `components/biohub/ReportStatus.tsx`)**: Panel de consumo por minuto y estado de reportes en segundo plano.

## Cómo ejecutar el proyecto

```bash
cd services/web-app
npm install
npm run dev
```

Abre `http://localhost:3000`.

### Verificación de calidad

- **Linter (ESLint):** `npm run lint`
- **Compilación de producción:** `npm run build`
- **Pruebas E2E (Playwright a 1440 px y 375 px):** `npm run test:e2e`
