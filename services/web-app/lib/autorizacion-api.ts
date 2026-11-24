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

// --- Acceso a la ubicación exacta por especie (ubicacion:administrar) ---

/** Especies que un usuario ve con coordenadas exactas: todas (ubicacion:ver_exacta) o las que se le dieron. */
export interface AccesoUbicacion {
  usuarioId: string | null;
  todas: boolean;
  especies: string[];
}

export interface AccesoUbicacionVigente {
  usuarioId: string;
  nombreUsuario: string;
  especieId: string;
  otorgadoPor: string;
  otorgadoEn: string;
}

export interface CambioAccesoUbicacion {
  id: number;
  usuarioId: string;
  nombreUsuario: string;
  especieId: string;
  accion: 'OTORGADO' | 'REVOCADO';
  realizadoPor: string;
  realizadoEn: string;
}

/** Si autorización no responde se asume el acceso más restrictivo (ninguna especie). */
export async function obtenerAccesoUbicacion(usuarioId: string): Promise<AccesoUbicacion> {
  try {
    const res = await fetch(`${API_BASE}/usuarios/${encodeURIComponent(usuarioId)}/ubicacion`, {
      cache: 'no-store',
    });
    if (res.ok) return res.json();
  } catch {
    // servicio caído
  }
  return { usuarioId, todas: false, especies: [] };
}

export function listarAccesosUbicacion(solicitanteId: string): Promise<AccesoUbicacionVigente[]> {
  return pedirComoAdmin<AccesoUbicacionVigente[]>(solicitanteId, '/ubicacion/accesos');
}

export function historialAccesosUbicacion(solicitanteId: string): Promise<CambioAccesoUbicacion[]> {
  return pedirComoAdmin<CambioAccesoUbicacion[]>(solicitanteId, '/ubicacion/historial');
}

export function usuariosParaUbicacion(solicitanteId: string): Promise<ResumenUsuario[]> {
  return pedirComoAdmin<ResumenUsuario[]>(solicitanteId, '/ubicacion/usuarios');
}

export function otorgarAccesoUbicacion(
  solicitanteId: string,
  usuarioId: string,
  especieId: string
): Promise<AccesoUbicacionVigente> {
  return pedirComoAdmin<AccesoUbicacionVigente>(solicitanteId, '/ubicacion/accesos', {
    method: 'POST',
    body: JSON.stringify({ usuarioId, especieId }),
  });
}

/** DELETE responde 204 sin cuerpo, por eso no usa pedirComoAdmin. */
export async function revocarAccesoUbicacion(
  solicitanteId: string,
  usuarioId: string,
  especieId: string
): Promise<void> {
  let res: Response;
  try {
    res = await fetch(
      `${API_BASE}/ubicacion/accesos/${encodeURIComponent(usuarioId)}/${encodeURIComponent(especieId)}`,
      { method: 'DELETE', headers: { 'X-Usuario-Id': solicitanteId }, cache: 'no-store' }
    );
  } catch {
    throw new ErrorAdministracion('No se pudo conectar con el servicio de autorización.');
  }
  if (!res.ok) {
    throw new ErrorAdministracion(await leerMensaje(res, 'No se pudo quitar el acceso.'));
  }
}

// --- Roles de las cuentas e historial de cambios (roles:administrar) ---

export type AccionRol = 'ROL_ASIGNADO' | 'ROL_QUITADO' | 'ROL_CREADO' | 'PERMISOS_CAMBIADOS';

export interface CambioRol {
  id: number;
  accion: AccionRol;
  usuarioId: string | null;
  rol: string;
  detalle: string | null;
  realizadoPor: string;
  realizadoEn: string;
}

/** POST y DELETE de roles responden 204 sin cuerpo, por eso no usan pedirComoAdmin. */
async function cambiarRolDeUsuario(
  metodo: 'POST' | 'DELETE',
  adminId: string,
  usuarioId: string,
  rol: string
): Promise<void> {
  let res: Response;
  try {
    res = await fetch(
      `${API_BASE}/usuarios/${encodeURIComponent(usuarioId)}/roles/${encodeURIComponent(rol)}`,
      { method: metodo, headers: { 'X-Usuario-Id': adminId }, cache: 'no-store' }
    );
  } catch {
    throw new ErrorAdministracion('No se pudo conectar con el servicio de autorización.');
  }
  if (!res.ok) {
    throw new ErrorAdministracion(
      await leerMensaje(res, metodo === 'POST' ? 'No se pudo asignar el rol.' : 'No se pudo quitar el rol.')
    );
  }
}

/**
 * Deja la cuenta con un solo rol. Primero se asigna el nuevo y luego se quitan los demás: si algo falla
 * a la mitad, la cuenta no se queda sin acceso. Se leen los roles guardados porque CIUDADANO puede
 * aparecer por defecto, sin estar asignado, y en ese caso no hay nada que quitar.
 */
export async function cambiarRol(adminId: string, usuarioId: string, rol: string): Promise<void> {
  const rolesDe = async () =>
    (await listarUsuarios(adminId)).find((u) => u.id === usuarioId)?.roles ?? [];

  if (!(await rolesDe()).includes(rol)) {
    await cambiarRolDeUsuario('POST', adminId, usuarioId, rol);
  }
  const sobrantes = (await rolesDe()).filter((r) => r !== rol);
  try {
    for (const anterior of sobrantes) {
      await cambiarRolDeUsuario('DELETE', adminId, usuarioId, anterior);
    }
  } catch (error) {
    const motivo = error instanceof Error ? ` ${error.message}` : '';
    throw new ErrorAdministracion(`El rol nuevo quedó asignado, pero no se pudo quitar el anterior.${motivo}`);
  }
}

export function historialRoles(adminId: string): Promise<CambioRol[]> {
  return pedirComoAdmin<CambioRol[]>(adminId, '/roles/historial');
}
