import type { NextConfig } from 'next';

// URL de servicio-registro. Por defecto el registro local (mvnw o docker compose en :8081).
const REGISTRO_URL = process.env.REGISTRO_URL ?? 'http://localhost:8081';

const nextConfig: NextConfig = {
  reactStrictMode: true,
  // El navegador llama a /api/registro/... (mismo origen, sin CORS) y Next lo reenvia a registro.
  async rewrites() {
    return [
      {
        source: '/api/registro/:path*',
        destination: `${REGISTRO_URL}/:path*`,
      },
    ];
  },
};

export default nextConfig;
