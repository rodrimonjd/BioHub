const API_BASE = '/api/reportes';

export type EstadoReporte = 'EN_COLA' | 'GENERANDO' | 'LISTO' | 'SIN_DATOS' | 'ERROR';

export interface Reporte {
  id: string;
  fechaInicio: string;
  fechaFin: string;
  departamento: string | null;
  especieId: string | null;
  estado: EstadoReporte;
  solicitadoPor: string;
  mensaje: string | null;
  totalAvistamientos: number;
  totalIndividuos: number;
  creadoEn: string;
  generadoEn: string | null;
}

export interface SolicitudReporte {
  fechaInicio: string;
  fechaFin: string;
  departamento?: string;
  solicitadoPor: string;
}

/** El servicio de reportes exige el usuario (reporte:ver para consultar, reporte:generar para pedir). */
function cabeceraUsuario(usuarioId: string): Record<string, string> {
  return { 'X-Usuario-Id': usuarioId };
}

async function mensajeDeError(res: Response, porDefecto: string): Promise<string> {
  try {
    const cuerpo = await res.json();
    if (cuerpo?.message) return cuerpo.message;
  } catch {
    // respuesta sin JSON
  }
  return porDefecto;
}

/** Pide un reporte nuevo. El servicio responde 202 con el reporte EN_COLA. */
export async function solicitarReporte(solicitud: SolicitudReporte): Promise<Reporte> {
  const res = await fetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...cabeceraUsuario(solicitud.solicitadoPor) },
    body: JSON.stringify(solicitud),
  });
  if (!res.ok) {
    throw new Error(await mensajeDeError(res, 'No se pudo solicitar el reporte'));
  }
  return res.json();
}

export async function obtenerReporte(id: string, usuarioId: string): Promise<Reporte> {
  const res = await fetch(`${API_BASE}/${id}`, { cache: 'no-store', headers: cabeceraUsuario(usuarioId) });
  if (!res.ok) {
    throw new Error(await mensajeDeError(res, 'No se pudo consultar el reporte'));
  }
  return res.json();
}

/** Último reporte pedido por el usuario, o null si no tiene o el servicio no responde. */
export async function ultimoReporteDe(usuarioId: string): Promise<Reporte | null> {
  try {
    const res = await fetch(`${API_BASE}?solicitadoPor=${encodeURIComponent(usuarioId)}`, {
      cache: 'no-store',
      headers: cabeceraUsuario(usuarioId),
    });
    if (!res.ok) return null;
    const reportes: Reporte[] = await res.json();
    let ultimo: Reporte | null = null;
    for (const reporte of reportes) {
      if (!ultimo || reporte.creadoEn > ultimo.creadoEn) ultimo = reporte;
    }
    return ultimo;
  } catch {
    return null;
  }
}

/** Descarga el PDF del reporte; se pide con fetch porque un enlace no puede enviar la cabecera del usuario. */
export async function descargarPdf(id: string, usuarioId: string): Promise<void> {
  const res = await fetch(`${API_BASE}/${id}/exportar?formato=pdf`, {
    cache: 'no-store',
    headers: cabeceraUsuario(usuarioId),
  });
  if (!res.ok) {
    throw new Error(await mensajeDeError(res, 'No se pudo descargar el reporte'));
  }
  const url = URL.createObjectURL(await res.blob());
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = `reporte-${id}.pdf`;
  enlace.click();
  URL.revokeObjectURL(url);
}

/** Mientras esté en cola o generando hay que seguir consultando el estado. */
export function sigueEnProceso(estado: EstadoReporte): boolean {
  return estado === 'EN_COLA' || estado === 'GENERANDO';
}
