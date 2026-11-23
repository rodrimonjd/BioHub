import type { NextConfig } from 'next';

// URL de servicio-registro. Por defecto el registro local (mvnw o docker compose en :8081).
const REGISTRO_URL = process.env.REGISTRO_URL ?? process.env.REGISTRO_SERVICE_URL ?? 'http://localhost:8081';

// URL de servicio-taxonomia (fichas y atributos). Por defecto :8084.
const TAXONOMIA_URL = process.env.TAXONOMIA_URL ?? 'http://localhost:8084';

const nextConfig: NextConfig = {
  reactStrictMode: true, async rewrites() {
    return [
      {
        source: '/api/registro/:path*',
        destination: `${REGISTRO_URL}/:path*`,
      },
      {
        source: '/api/taxonomia/:path*',
        destination: `${TAXONOMIA_URL}/:path*`,
      },
    ];
  },
};

export default nextConfig;
