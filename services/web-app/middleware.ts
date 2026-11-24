import { NextRequest, NextResponse } from 'next/server';
import { COOKIE_SESION } from '@/lib/cookie-sesion';

/** Cabeceras de identidad que el navegador no puede escribir: la identidad sale solo del token de la cookie. */
const RESERVADAS = ['authorization', 'x-usuario-id', 'x-secreto-servicio', 'cookie'];

/**
 * Pasa el token de la cookie HttpOnly a los servicios como "Authorization: Bearer". Cada servicio lo verifica
 * con la clave pública de servicio-autorizacion (HU-8.2).
 */
export function middleware(solicitud: NextRequest) {
  const ruta = solicitud.nextUrl.pathname;
  // El token no se entrega al navegador: se inicia sesión por /api/sesion, que lo guarda en la cookie
  if (ruta === '/api/autorizacion/sesiones') {
    return NextResponse.json({ estado: 404, error: 'Not Found', mensaje: 'Recurso no encontrado' }, { status: 404 });
  }

  const cabeceras = new Headers(solicitud.headers);
  RESERVADAS.forEach((nombre) => cabeceras.delete(nombre));
  const token = solicitud.cookies.get(COOKIE_SESION)?.value;
  if (token && !ruta.startsWith('/api/externa/')) cabeceras.set('authorization', `Bearer ${token}`);
  return NextResponse.next({ request: { headers: cabeceras } });
}

export const config = { matcher: '/api/:path*' };
