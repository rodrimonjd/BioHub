import type { ConservationStatus } from '@/components/ui/ConservationBadge';
import type { HallazgoResponse } from '@/lib/registro-api';

const API_BASE = '/api/consulta';

/** Máximo de hallazgos por página que acepta servicio-consulta-publica. */
const TAMANO_MAXIMO = 100;

/** Lo guardado en registro llega por eventos en unos segundos: cuándo volver a consultar después de un cambio. */
export const RECONSULTAS_MS = [1500, 4000, 7000, 10000];

/** Hallazgo del modelo de lectura: solo lo que la cuenta puede ver según servicio-autorizacion. */
export interface HallazgoPublico {
  id: string;
  especieId: string | null;
  especie: string;
  nombreComun: string | null;
  categoriaConservacion: ConservationStatus | null;
  fecha: string;
  descripcion: string;
  departamento: string;
  region: string | null;
  latitud: number | null;
  longitud: number | null;
  nivelUbicacion: 'EXACTA' | 'REGION' | 'DEPARTAMENTO';
  version: number;
}

interface PaginaHallazgos {
  contenido: HallazgoPublico[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface FiltroHallazgos {
  /** Nombre científico o común. */
  especie?: string;
  /** Palabras clave: cada una debe aparecer en la especie, la descripción o el departamento. */
  texto?: string;
  /** Día AAAA-MM-DD, incluido. */
  fechaDesde?: string;
  /** Día AAAA-MM-DD, incluido. */
  fechaHasta?: string;
}

export class ErrorConsulta extends Error {}

async function pedirPagina(
  filtro: FiltroHallazgos,
  pagina: number,
  tamano: number,
  usuarioId: string,
  signal?: AbortSignal
): Promise<PaginaHallazgos> {
  const parametros = new URLSearchParams({ pagina: String(pagina), tamano: String(tamano) });
  for (const [nombre, valor] of Object.entries(filtro)) {
    if (valor?.trim()) parametros.set(nombre, valor.trim());
  }
  let res: Response;
  try {
    res = await fetch(`${API_BASE}/hallazgos?${parametros}`, {
      cache: 'no-store',
      headers: { 'X-Usuario-Id': usuarioId },
      signal,
    });
  } catch (error) {
    if (signal?.aborted) throw error;
    throw new ErrorConsulta('No se pudo conectar con la consulta pública.');
  }
  if (!res.ok) {
    const cuerpo = await res.json().catch(() => null);
    throw new ErrorConsulta(
      res.status === 400 && cuerpo?.mensaje ? cuerpo.mensaje : 'La consulta pública no está disponible.'
    );
  }
  return res.json();
}

/** Los `cantidad` hallazgos más recientes que coinciden con el filtro, y cuántos hay en total. */
export async function consultarHallazgos(
  filtro: FiltroHallazgos,
  cantidad: number,
  usuarioId: string,
  signal?: AbortSignal
): Promise<{ hallazgos: HallazgoPublico[]; total: number }> {
  const tamano = Math.min(cantidad, TAMANO_MAXIMO);
  const primera = await pedirPagina(filtro, 0, tamano, usuarioId, signal);
  const hallazgos = [...primera.contenido];
  const paginas = Math.min(Math.ceil(cantidad / tamano), primera.totalPaginas);
  for (let pagina = 1; pagina < paginas; pagina++) {
    hallazgos.push(...(await pedirPagina(filtro, pagina, tamano, usuarioId, signal)).contenido);
  }
  return { hallazgos: hallazgos.slice(0, cantidad), total: primera.totalElementos };
}

/** Todos los hallazgos públicos con la forma de registro, sin los datos internos. */
export async function listarHallazgosPublicos(usuarioId: string): Promise<{
  data: HallazgoResponse[] | null;
  online: boolean;
}> {
  try {
    const { hallazgos } = await consultarHallazgos({}, 1000, usuarioId);
    return {
      online: true,
      data: hallazgos.map((h) => ({
        id: h.id,
        investigadorId: '',
        especieId: h.especieId ?? '',
        especie: h.especie,
        fichaId: null,
        fecha: h.fecha,
        descripcion: h.descripcion,
        latitud: h.latitud,
        longitud: h.longitud,
        departamento: h.departamento,
        region: h.region,
        versionActual: h.version,
        creadoEn: h.fecha,
        evidencias: [],
      })),
    };
  } catch {
    return { data: null, online: false };
  }
}
