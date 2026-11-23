// Cliente de servicio-autorizacion (a través del proxy /api/autorizacion de next.config.ts).

const API_BASE = '/api/autorizacion';

export interface RespuestaSesion {
  usuarioId: string;
  nombre: string;
  correo: string;
  roles: string[];
  permisos: string[];
}

export interface AccesoUsuario {
  usuarioId: string;
  roles: string[];
  permisos: string[];
}

export interface ResumenUsuario {
  id: string;
  nombre: string;
}

/** Error de inicio de sesión: credenciales incorrectas o servicio no disponible. */
export class ErrorSesion extends Error {
  constructor(
    message: string,
    public readonly servicioCaido: boolean
  ) {
    super(message);
  }
}

async function leerMensaje(res: Response, porDefecto: string): Promise<string> {
  try {
    const cuerpo = await res.json();
    if (cuerpo?.mensaje) return cuerpo.mensaje;
  } catch {
    // respuesta sin JSON
  }
  return porDefecto;
}

/** POST /sesiones: usuario (id o correo) y contraseña. */
export async function iniciarSesion(usuario: string, clave: string): Promise<RespuestaSesion> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}/sesiones`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usuario, clave }),
    });
  } catch {
    throw new ErrorSesion('No se pudo conectar con el servicio de autorización.', true);
  }
  if (res.status === 401 || res.status === 400) {
    throw new ErrorSesion(await leerMensaje(res, 'Usuario o contraseña incorrectos'), false);
  }
  if (!res.ok) {
    throw new ErrorSesion('El servicio de autorización no está disponible en este momento.', true);
  }
  return res.json();
}

/** Roles y permisos vigentes de un usuario (para refrescar la sesión guardada). */
export async function obtenerAcceso(usuarioId: string): Promise<AccesoUsuario | null> {
  try {
    const res = await fetch(`${API_BASE}/usuarios/${encodeURIComponent(usuarioId)}/permisos`, {
      cache: 'no-store',
    });
    return res.ok ? res.json() : null;
  } catch {
    return null;
  }
}

// ---- Administración de cuentas (solo quien tiene roles:administrar) ----

export interface VistaRol {
  nombre: string;
  descripcion: string | null;
  permisos: string[];
}

export interface VistaUsuario {
  id: string;
  nombre: string;
  correo: string;
  activo: boolean;
  roles: string[];
}

export interface SolicitudCrearUsuario {
  id: string;
  nombre: string;
  correo: string;
  clave: string;
  rol: string | null;
}

/** Error al administrar cuentas, con el mensaje que devuelve autorización (p. ej. "Ya existe un usuario..."). */
export class ErrorAdministracion extends Error {}

async function pedirComoAdmin<T>(
  adminId: string,
  ruta: string,
  opciones: RequestInit = {}
): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}${ruta}`, {
      ...opciones,
      headers: { 'Content-Type': 'application/json', 'X-Usuario-Id': adminId, ...opciones.headers },
      cache: 'no-store',
    });
  } catch {
    throw new ErrorAdministracion('No se pudo conectar con el servicio de autorización.');
  }
  if (!res.ok) {
    throw new ErrorAdministracion(
      await leerMensaje(res, 'El servicio de autorización no pudo completar la operación.')
    );
  }
  return res.json();
}

export function listarRoles(adminId: string): Promise<VistaRol[]> {
  return pedirComoAdmin<VistaRol[]>(adminId, '/roles');
}

export function listarUsuarios(adminId: string): Promise<VistaUsuario[]> {
  return pedirComoAdmin<VistaUsuario[]>(adminId, '/usuarios');
}

export function crearUsuario(adminId: string, datos: SolicitudCrearUsuario): Promise<VistaUsuario> {
  return pedirComoAdmin<VistaUsuario>(adminId, '/usuarios', {
    method: 'POST',
    body: JSON.stringify(datos),
  });
}

/** Cuentas que tienen un permiso; requiere la sesión del usuario que consulta. */
export async function listarUsuariosConPermiso(
  usuarioId: string,
  permiso: string
): Promise<ResumenUsuario[]> {
  try {
    const res = await fetch(`${API_BASE}/usuarios/con-permiso/${encodeURIComponent(permiso)}`, {
      headers: { 'X-Usuario-Id': usuarioId },
      cache: 'no-store',
    });
    return res.ok ? res.json() : [];
  } catch {
    return [];
  }
}
