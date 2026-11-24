import { clearStoredSession } from '@/lib/permissions';

/** Borra la cookie con el token en el servidor. */
export async function borrarCookieSesion(): Promise<void> {
  try {
    await fetch('/api/sesion', { method: 'DELETE', keepalive: true });
  } catch {
    // sin conexión: el token vence solo
  }
}

/** Cierra la sesión: olvida los datos de la cuenta en este navegador y borra el token. */
export function cerrarSesion(): Promise<void> {
  clearStoredSession();
  return borrarCookieSesion();
}
