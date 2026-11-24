/** Se dispara cuando un servicio rechaza el token: la sesión venció o ya no es válida. */
export const EVENTO_SESION_VENCIDA = 'biohub:sesion-vencida';

/**
 * fetch a los servicios. La identidad va sola en la cookie HttpOnly (la agrega middleware.ts), así que aquí
 * no se envía ninguna cabecera de usuario.
 */
export async function pedir(ruta: string, opciones: RequestInit = {}): Promise<Response> {
  const res = await fetch(ruta, opciones);
  if (res.status === 401) window.dispatchEvent(new Event(EVENTO_SESION_VENCIDA));
  return res;
}
