// Cliente de servicio-historial (a través del proxy /api/historial de next.config.ts).
// Todas las consultas exigen X-Usuario-Id con el permiso historial:ver.

const API_BASE = '/api/historial';

export type TipoRegistroHistorial = 'hallazgos' | 'fichas';
export type TipoCambio = 'CREACION' | 'MODIFICACION' | 'RECLASIFICACION' | 'DESACTIVACION';

export interface CambioHistorial {
  version: number;
  tipoCambio: TipoCambio;
  campo: string | null;
  valorAnterior: string | null;
  valorNuevo: string | null;
  motivo: string | null;
  autor: string;
  autorizador: string | null;
  ocurrioEn: string;
  registradoEn: string;
  descripcion: string;
}

export interface Historial {
  tipoRegistro: string;
  idRegistro: string;
  versionActual: number;
  totalModificaciones: number;
  completo: boolean;
  versionesFaltantes: number[];
  mensaje: string | null;
  cambios: CambioHistorial[];
}

/** Error al consultar el historial, con el mensaje del servicio y si es falta de permiso o registro sin historial. */
export class ErrorHistorial extends Error {
  constructor(
    message: string,
    public readonly status: number | null
  ) {
    super(message);
  }
}

export async function obtenerHistorial(
  tipo: TipoRegistroHistorial,
  id: string,
  usuarioId: string
): Promise<Historial> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}/${tipo}/${encodeURIComponent(id)}`, {
      headers: { 'X-Usuario-Id': usuarioId },
      cache: 'no-store',
    });
  } catch {
    throw new ErrorHistorial('No se pudo conectar con el servicio de historial.', null);
  }
  if (!res.ok) {
    let mensaje = 'El servicio de historial no está disponible en este momento.';
    try {
      const cuerpo = await res.json();
      mensaje = cuerpo?.mensaje ?? cuerpo?.message ?? mensaje;
    } catch {
      // respuesta sin JSON
    }
    if (res.status === 404) mensaje = 'Este registro todavía no tiene cambios en el historial.';
    throw new ErrorHistorial(mensaje, res.status);
  }
  return res.json();
}
