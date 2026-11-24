import type { NextConfig } from 'next';

// URLs de los servicios. Por defecto los puertos locales (mvnw o docker compose).
const REGISTRO_URL =
  process.env.REGISTRO_URL ??
  process.env.REGISTRO_SERVICE_URL ??
  'http://localhost:8081';
const AUTORIZACION_URL = process.env.AUTORIZACION_URL ?? 'http://localhost:8082';
// servicio-taxonomia (fichas y atributos).
const TAXONOMIA_URL = process.env.TAXONOMIA_URL ?? 'http://localhost:8084';

// URL de servicio-reportes (reportes en segundo plano). Por defecto :8085.
const REPORTES_URL = process.env.REPORTES_URL ?? 'http://localhost:8085';

// URL de servicio-historial (cambios y versiones inmutables). Por defecto :8086.
const HISTORIAL_URL = process.env.HISTORIAL_URL ?? 'http://localhost:8086';

const GATEWAY_URL = process.env.GATEWAY_URL ?? 'http://localhost:8080';

const nextConfig: NextConfig = {
  reactStrictMode: true,
  // Genera un servidor autocontenido (.next/standalone) para la imagen de Docker
  output: 'standalone',
  // Oculta el botón "N" de herramientas de desarrollo de Next en la esquina inferior izquierda
  devIndicators: false,
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
      {
        source: '/api/taxonomia/:path*',
        destination: `${TAXONOMIA_URL}/:path*`,
      },
      {
        source: '/api/reportes/:path*',
        destination: `${REPORTES_URL}/reportes/:path*`,
      },
      {
        source: '/api/historial/:path*',
        destination: `${HISTORIAL_URL}/api/v1/historial/:path*`,
      },
      {
        source: '/api/reportes',
        destination: `${REPORTES_URL}/reportes`,
      },
      {
        source: '/api/gateway/consumo',
        destination: `${GATEWAY_URL}/gateway/consumo`,
      },
      {
        source: '/api/externa/:path*',
        destination: `${GATEWAY_URL}/api/v1/:path*`,
      },
    ];
  },
};

export default nextConfig;
