import type { NextConfig } from 'next';

<<<<<<< HEAD
// URLs de los servicios. Por defecto los puertos locales (mvnw o docker compose).
const REGISTRO_URL =
  process.env.REGISTRO_URL ??
  process.env.REGISTRO_SERVICE_URL ??
  'http://localhost:8081';
const AUTORIZACION_URL = process.env.AUTORIZACION_URL ?? 'http://localhost:8082';

const nextConfig: NextConfig = {
  reactStrictMode: true,
  // El navegador llama a /api/<servicio>/... (mismo origen, sin CORS) y Next lo reenvia al servicio.
  async rewrites() {
=======
// URL de servicio-registro. Por defecto el registro local (mvnw o docker compose en :8081).
const REGISTRO_URL = process.env.REGISTRO_URL ?? process.env.REGISTRO_SERVICE_URL ?? 'http://localhost:8081';

// URL de servicio-taxonomia (fichas y atributos). Por defecto :8084.
const TAXONOMIA_URL = process.env.TAXONOMIA_URL ?? 'http://localhost:8084';

const nextConfig: NextConfig = {
  reactStrictMode: true, async rewrites() {
>>>>>>> origin/desarrollo
    return [
      {
        source: '/api/registro/:path*',
        destination: `${REGISTRO_URL}/:path*`,
      },
      {
<<<<<<< HEAD
        source: '/api/autorizacion/:path*',
        destination: `${AUTORIZACION_URL}/api/v1/autorizacion/:path*`,
=======
        source: '/api/taxonomia/:path*',
        destination: `${TAXONOMIA_URL}/:path*`,
>>>>>>> origin/desarrollo
      },
    ];
  },
};

export default nextConfig;
