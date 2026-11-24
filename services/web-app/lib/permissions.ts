import type { AccesoUsuario, RespuestaSesion } from '@/lib/autorizacion-api';

/**
 * Sesión del usuario en el navegador. Los roles y permisos vienen de servicio-autorizacion:
 * el front no decide quién puede qué, solo muestra u oculta acciones según esos permisos.
 */
export interface BioHubUser {
  id: string;
  name: string;
  initials: string;
  email: string;
  roles: string[];
  permisos: string[];
  roleLabel: string;
  institution: string;
  station: string;
  /** Sesión de invitado: sin cuenta ni contraseña, con los permisos de CIUDADANO. */
  invitado?: boolean;
}

/** Id con el que el invitado consulta sus permisos; servicio-autorizacion no permite crear una cuenta con él. */
export const ID_INVITADO = 'invitado';

/** Códigos de permiso de servicio-autorizacion que usa la interfaz. */
export const PERMISOS = {
  CREAR_HALLAZGO: 'hallazgo:crear',
  EDITAR_HALLAZGO: 'hallazgo:editar',
  VALIDAR_HALLAZGO: 'hallazgo:validar',
  VER_UBICACION_EXACTA: 'ubicacion:ver_exacta',
  VER_HISTORIAL: 'historial:ver',
  VER_REPORTES: 'reporte:ver',
  GENERAR_REPORTES: 'reporte:generar',
  GESTIONAR_EQUIPOS: 'equipo:gestionar',
  ADMINISTRAR_ROLES: 'roles:administrar',
  EDITAR_FICHA: 'ficha:editar',
  MONITOREAR_API: 'api_externa:monitorear',
  CONFIGURAR_API: 'api_externa:configurar',
} as const;

const ETIQUETAS_ROL: Record<string, string> = {
  ADMIN: 'Administrador del sistema',
  AUTORIDAD_AMBIENTAL: 'Autoridad ambiental',
  INVESTIGADOR: 'Investigador de campo',
  CIUDADANO: 'Visitante común',
  CONSUMIDOR_EXTERNO: 'Consumidor externo',
};

// Orden para elegir el rol que se muestra cuando el usuario tiene varios.
const PRIORIDAD_ROL = ['ADMIN', 'AUTORIDAD_AMBIENTAL', 'INVESTIGADOR', 'CONSUMIDOR_EXTERNO', 'CIUDADANO'];

export function etiquetaRoles(roles: string[]): string {
  const principal = [...roles].sort(
    (a, b) => indicePrioridad(a) - indicePrioridad(b)
  )[0];
  return principal ? ETIQUETAS_ROL[principal] ?? principal : 'Sin rol asignado';
}

function indicePrioridad(rol: string): number {
  const i = PRIORIDAD_ROL.indexOf(rol);
  return i === -1 ? PRIORIDAD_ROL.length : i;
}

function iniciales(nombre: string): string {
  const partes = nombre.trim().split(/\s+/).filter(Boolean);
  return (partes[0]?.[0] ?? '?').concat(partes[1]?.[0] ?? '').toUpperCase();
}

/** Convierte la respuesta de POST /sesiones en la sesión del front. */
export function crearSesion(r: RespuestaSesion): BioHubUser {
  return {
    id: r.usuarioId,
    name: r.nombre,
    initials: iniciales(r.nombre),
    email: r.correo,
    roles: r.roles,
    permisos: r.permisos,
    roleLabel: etiquetaRoles(r.roles),
    institution: 'Instituto Nacional de Biodiversidad',
    station: 'Panel científico BioHub',
  };
}

/**
 * Sesión de invitado. Los permisos vienen de servicio-autorizacion (un usuario sin roles es CIUDADANO);
 * si no responde, se usan los de CIUDADANO, que solo permiten consultar.
 */
export function crearSesionInvitado(acceso: AccesoUsuario | null): BioHubUser {
  const roles = acceso?.roles ?? ['CIUDADANO'];
  return {
    id: ID_INVITADO,
    name: 'Invitado',
    initials: 'IN',
    email: '',
    roles,
    permisos: acceso?.permisos ?? ['hallazgo:ver'],
    roleLabel: etiquetaRoles(roles),
    institution: 'Consulta abierta ciudadana',
    station: 'Acceso como invitado',
    invitado: true,
  };
}

/** Actualiza roles y permisos de una sesión guardada con lo que diga hoy el backend. */
export function actualizarAcceso(user: BioHubUser, acceso: AccesoUsuario): BioHubUser {
  return { ...user, roles: acceso.roles, permisos: acceso.permisos, roleLabel: etiquetaRoles(acceso.roles) };
}

export function tienePermiso(user: BioHubUser | null | undefined, permiso: string): boolean {
  return !!user && user.permisos.includes(permiso);
}

const SESSION_STORAGE_KEY = 'biohub_active_session_v2';

export function getStoredSession(): BioHubUser | null {
  if (typeof window === 'undefined') return null;
  try {
    const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as BioHubUser;
    return Array.isArray(parsed?.permisos) ? parsed : null;
  } catch {
    return null;
  }
}

export function saveStoredSession(user: BioHubUser): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(user));
  } catch {
    // ignore storage errors
  }
}

export function clearStoredSession(): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    window.localStorage.removeItem('biohub_active_session_v1'); // sesión demo antigua
  } catch {
    // ignore storage errors
  }
}

/** Coordenadas exactas de especies sensibles: permiso ubicacion:ver_exacta. */
export function canViewExactCoordinates(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.VER_UBICACION_EXACTA);
}

/** Registrar avistamientos y hallazgos: permiso hallazgo:crear. */
export function canRegisterFieldData(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.CREAR_HALLAZGO);
}

/** Modificar y reclasificar hallazgos: permiso hallazgo:editar. */
export function canEditFindings(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.EDITAR_HALLAZGO);
}

/** Agregar o cambiar atributos de fichas taxonómicas: permiso ficha:editar (el invitado no lo tiene). */
export function canEditFichas(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.EDITAR_FICHA);
}

/** Autorizar reclasificaciones: permiso hallazgo:validar. */
export function canAuthorizeReclassification(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.VALIDAR_HALLAZGO);
}

/** Ver reportes científicos: permiso reporte:ver (el invitado no lo tiene). */
export function canViewReports(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.VER_REPORTES);
}

/** Ver el consumo de la API externa: permiso api_externa:monitorear (el invitado no lo tiene). */
export function canViewApiUsage(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.MONITOREAR_API);
}

export function canConfigureApi(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.CONFIGURAR_API);
}

/** Solicitar reportes científicos: permiso reporte:generar. */
export function canGenerateReports(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.GENERAR_REPORTES);
}

export function canManageEquipment(user: BioHubUser | null | undefined): boolean {
  return tienePermiso(user, PERMISOS.GESTIONAR_EQUIPOS);
}
