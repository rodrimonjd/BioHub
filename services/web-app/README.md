# BioHub — Sitio Web Oficial (`services/web-app`)

Interfaz web de **BioHub — Sistema Nacional de Registro y Vigilancia de Biodiversidad** construida con **Next.js (App Router) + TypeScript + Tailwind CSS v4** bajo la dirección estética **Editorial Naturalista**.

## Arquitectura del Frontend

- **`app/globals.css`**: Tokens de diseño en `@theme` de Tailwind v4 (`paper`, `surface`, `line`, `ink`, `ink-muted`, `forest-900`, `forest-700`, `moss-500`, `water-600`, `sun-500` y estados UICN `iucn-lc`, `iucn-nt`, `iucn-vu`, `iucn-en`, `iucn-cr`), soporte de modo oscuro (`.dark`) y respeto por `prefers-reduced-motion`.
- **`app/layout.tsx`**: Configuración tipográfica con `next/font/google` (`Fraunces` para títulos y nombres científicos en cursiva, `Inter` para interfaz/cuerpo, `JetBrains Mono` para datos técnicos).
- **`lib/permissions.ts`**: Función única centralizada (`canViewExactCoordinates`) que decide si un rol (`"public"` | `"senior"`) puede visualizar coordenadas exactas de especies sensibles.
- **`components/ui/`**:
  - `Button.tsx` (`primary`, `secondary`, `ghost`)
  - `Card.tsx`
  - `Container.tsx` (`max-w-6xl`)
  - `Section.tsx` (`py-20`)
  - `ConservationBadge.tsx` (`LC` | `NT` | `VU` | `EN` | `CR`, con contraste AA garantizado —`VU` siempre con texto `ink`— y sigla visible).
- **`components/biohub/`**:
  - `SpeciesCard.tsx`: Lámina botánica en trazos finos SVG, nombre común, nombre científico en serif cursiva, `ConservationBadge` y atributos flexibles desde un arreglo `{ label, value }`.
  - `HistoryTimeline.tsx`: Línea de tiempo vertical inmutable con fecha e ID en monoespaciada, qué se creía, qué se sabe ahora, quién autorizó el cambio y por qué.
  - `ProtectedLocation.tsx`: Mapa topográfico difuminado con candado (`"Región: Meta"`) para visitante común y coordenadas exactas en monoespaciada para personal científico de alto rango.
  - `ApiUsagePanel.tsx`: Barra de peticiones por minuto (`moss-500` normal, `sun-500` al superar el 80%, `iucn-cr` al exceder el tope).
  - `ReportStatus.tsx`: Indicador asíncrono en segundo plano con estados `en_cola`, `generando` y `listo`.

## Cómo correr el proyecto

```bash
cd services/web-app
npm install
npm run dev
```

Luego abre `http://localhost:3000` en el navegador. Para compilar para producción:

```bash
npm run build
npm run start
```
