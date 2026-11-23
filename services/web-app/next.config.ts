import type { NextConfig } from 'next';

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
    return [
      {
        source: '/api/registro/:path*',
        destination: `${REGISTRO_URL}/:path*`,
      },
      {
        source: '/api/autorizacion/:path*',
        destination: `${AUTORIZACION_URL}/api/v1/autorizacion/:path*`,
      },
    ];
  },
};

export default nextConfig;
