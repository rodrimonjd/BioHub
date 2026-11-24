/** Cookie HttpOnly con el token de la sesión: solo la leen el servidor de Next y su middleware. */
export const COOKIE_SESION = 'biohub_sesion';

/** Dura más que el token: así, cuando vence, el servicio responde 401 y la web puede avisar. */
export const DURACION_COOKIE_S = 30 * 24 * 60 * 60;
