import { NextRequest, NextResponse } from 'next/server';
import { COOKIE_SESION, DURACION_COOKIE_S } from '@/lib/cookie-sesion';

const AUTORIZACION_URL = process.env.AUTORIZACION_URL ?? 'http://localhost:8082';

function esDeOtroSitio(solicitud: NextRequest): boolean {
  const sitio = solicitud.headers.get('sec-fetch-site');
  return sitio === 'cross-site' || sitio === 'same-site';
}

function opcionesCookie(solicitud: NextRequest) {
  const protocolo = solicitud.headers.get('x-forwarded-proto') ?? solicitud.nextUrl.protocol;
  return {
    httpOnly: true,
    sameSite: 'strict' as const,
    secure: protocolo.startsWith('https'),
    path: '/',
  };
}

function error(estado: number, mensaje: string) {
  return NextResponse.json({ estado, mensaje }, { status: estado });
}

/** Inicia sesión en servicio-autorizacion y guarda el token en una cookie que el JavaScript no puede leer. */
export async function POST(solicitud: NextRequest) {
  if (esDeOtroSitio(solicitud)) return error(403, 'Solicitud no permitida');
  let res: Response;
  try {
    res = await fetch(`${AUTORIZACION_URL}/api/v1/autorizacion/sesiones`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: await solicitud.text(),
      cache: 'no-store',
      signal: AbortSignal.timeout(10_000),
    });
  } catch {
    return error(503, 'No se pudo conectar con el servicio de autorización.');
  }
  const datos = await res.json().catch(() => null);
  if (!res.ok) return error(res.status, datos?.mensaje ?? 'No se pudo iniciar sesión');
  if (typeof datos?.token !== 'string') {
    return error(502, 'El servicio de autorización no entregó una sesión.');
  }
  const { token, ...sesion } = datos;
  const respuesta = NextResponse.json(sesion);
  respuesta.cookies.set(COOKIE_SESION, token, { ...opcionesCookie(solicitud), maxAge: DURACION_COOKIE_S });
  return respuesta;
}

/** Cierra la sesión: borra la cookie con el token. */
export async function DELETE(solicitud: NextRequest) {
  if (esDeOtroSitio(solicitud)) return error(403, 'Solicitud no permitida');
  const respuesta = new NextResponse(null, { status: 204 });
  respuesta.cookies.set(COOKIE_SESION, '', { ...opcionesCookie(solicitud), maxAge: 0 });
  return respuesta;
}
