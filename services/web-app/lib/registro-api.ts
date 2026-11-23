import { ConservationStatus } from '@/components/ui/ConservationBadge';

export interface SeedSpecies {
  id: string;
  scientificName: string;
  commonName: string;
  status: ConservationStatus;
}

/**
 * Fuente única de verdad para las categorías UICN de las especies del catálogo.
 * - Crocodylus intermedius -> CR (En peligro crítico)
 * - Panthera onca -> NT (Casi amenazada)
 * - Ara macao -> LC (Preocupación menor)
 */
export const BACKEND_SEED_SPECIES: SeedSpecies[] = [
  {
    id: '11111111-1111-1111-1111-111111111111',
    scientificName: 'Crocodylus intermedius',
    commonName: 'Caimán del Orinoco',
    status: 'CR',
  },
  {
    id: '22222222-2222-2222-2222-222222222222',
    scientificName: 'Panthera onca',
    commonName: 'Jaguar',
    status: 'NT',
  },
  {
    id: '33333333-3333-3333-3333-333333333333',
    scientificName: 'Ara macao',
    commonName: 'Guacamaya roja',
    status: 'LC',
  },
];

export function getSpeciesByScientificName(
  scientificName: string
): SeedSpecies | undefined {
  return BACKEND_SEED_SPECIES.find(
    (s) => s.scientificName.toLowerCase() === scientificName.toLowerCase()
  );
}

export interface AvistamientoRequest {
  investigadorId: string;
  especieId: string;
  fechaHora: string;
  cantidad: number;
  latitud: number | null;
  longitud: number | null;
  departamento: string;
  region: string | null;
}

export interface AvistamientoResponse {
  id: string;
  especieId: string;
  especie: string;
  fechaHora: string;
  cantidad: number;
  latitud: number | null;
  longitud: number | null;
  departamento: string;
  region: string | null;
  creadoEn: string;
  pendingSync?: boolean;
  originalPayload?: AvistamientoRequest;
}

export type TipoEvidencia = 'TEMPERATURA' | 'ADN' | 'AUDIO';

export interface EvidenciaRequest {
  tipo: TipoEvidencia;
  valor?: string;
  unidad?: string;
  urlArchivo?: string;
  atributoTaxonomico?: string;
}

export interface EvidenciaResponse {
  id: string;
  tipo: TipoEvidencia;
  valor: string | null;
  unidad: string | null;
  urlArchivo: string | null;
  atributoTaxonomico: string | null;
}

export interface HallazgoRequest {
  investigadorId: string;
  especieId: string;
  fichaId?: string;
  fecha: string;
  descripcion: string;
  latitud: number | null;
  longitud: number | null;
  departamento: string;
  region: string | null;
  /** Expedición de servicio-registro en la que se hizo el hallazgo (opcional). */
  expedicionId?: string;
  evidencias: EvidenciaRequest[];
}

export interface Expedicion {
  id: string;
  nombre: string;
  zona: string;
  fechaInicio: string;
  fechaFin: string | null;
  liderId: string;
}

/** Expediciones registradas (GET /expediciones). Lista vacía si registro no responde. */
export async function listarExpediciones(): Promise<Expedicion[]> {
  try {
    const res = await fetch(`${API_BASE}/expediciones`, { cache: 'no-store' });
    return res.ok ? res.json() : [];
  } catch {
    return [];
  }
}

export interface HallazgoResponse {
  id: string;
  investigadorId: string;
  especieId: string;
  especie: string;
  fichaId: string | null;
  fecha: string;
  descripcion: string;
  latitud: number | null;
  longitud: number | null;
  departamento: string;
  region: string | null;
  versionActual: number;
  expedicionId?: string | null;
  creadoEn: string;
  evidencias: EvidenciaResponse[];
  pendingSync?: boolean;
}

export interface ModificarHallazgoRequest {
  campo: 'descripcion' | 'departamento' | 'region';
  valor: string;
  autor: string;
}

export interface ReclasificarHallazgoRequest {
  nuevaEspecieId: string;
  motivo: string;
  autor: string;
  autorizador: string;
}

const API_BASE = '/api/registro';

async function parseErrorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json();
    if (body?.message) return body.message;
    if (body?.error) return `${body.error}`;
  } catch {
    // ignore JSON parse error
  }
  return fallback;
}

export async function listarAvistamientos(): Promise<{
  data: AvistamientoResponse[] | null;
  online: boolean;
}> {
  try {
    const res = await fetch(`${API_BASE}/avistamientos`, { cache: 'no-store' });
    if (!res.ok) return { data: null, online: false };
    const data: AvistamientoResponse[] = await res.json();
    return { data, online: true };
  } catch {
    return { data: null, online: false };
  }
}

export async function listarHallazgos(): Promise<{
  data: HallazgoResponse[] | null;
  online: boolean;
}> {
  try {
    const res = await fetch(`${API_BASE}/hallazgos`, { cache: 'no-store' });
    if (!res.ok) return { data: null, online: false };
    const data: HallazgoResponse[] = await res.json();
    return { data, online: true };
  } catch {
    return { data: null, online: false };
  }
}

export async function obtenerAvistamientoPorId(id: string): Promise<AvistamientoResponse> {
  const res = await fetch(`${API_BASE}/avistamientos/${id}`, { cache: 'no-store' });
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res, 'Avistamiento no encontrado'));
  }
  return res.json();
}

/**
 * El servidor respondió pero rechazó la operación (sin permiso, datos inválidos, no encontrado).
 * No tiene sentido guardarla localmente para reintentar: hay que mostrar el motivo.
 * Cualquier otro error (sin conexión, 5xx) significa que el servidor no está disponible.
 */
export class ErrorRechazado extends Error {
  constructor(
    message: string,
    public readonly status: number
  ) {
    super(message);
  }
}

/**
 * Escritura en servicio-registro. Envía X-Usuario-Id: registro valida el permiso con servicio-autorizacion
 * y usa ese id como investigador/autor (ignora el que venga en el cuerpo).
 */
async function escribir(
  ruta: string,
  metodo: 'POST' | 'PATCH',
  payload: unknown,
  usuarioId: string,
  mensajeError: string
): Promise<Response> {
  const res = await fetch(`${API_BASE}${ruta}`, {
    method: metodo,
    headers: { 'Content-Type': 'application/json', 'X-Usuario-Id': usuarioId },
    body: JSON.stringify(payload),
  });
  if (res.ok) return res;
  const mensaje = await parseErrorMessage(res, mensajeError);
  if (res.status >= 400 && res.status < 500) {
    throw new ErrorRechazado(
      res.status === 403 ? `Sin permiso: ${mensaje}` : mensaje,
      res.status
    );
  }
  throw new Error(mensaje);
}

export interface EspecieNueva {
  nombreCientifico: string;
  nombreComun: string | null;
  estadoConservacion: ConservationStatus;
}

export interface EspecieCreada {
  id: string;
  nombreCientifico: string;
  nombreComun: string | null;
  estadoConservacion: ConservationStatus;
}

/** Agrega una especie al catálogo de servicio-registro (requiere hallazgo:crear). */
export async function crearEspecie(payload: EspecieNueva, usuarioId: string): Promise<EspecieCreada> {
  const res = await escribir('/especies', 'POST', payload, usuarioId,
    'Servicio de registro no disponible');
  return res.json();
}

export async function registrarAvistamiento(
  payload: AvistamientoRequest,
  usuarioId: string
): Promise<AvistamientoResponse> {
  const res = await escribir('/avistamientos', 'POST', payload, usuarioId,
    'Servicio de registro no disponible');
  return res.json();
}

export async function registrarHallazgo(
  payload: HallazgoRequest,
  usuarioId: string
): Promise<{ id: string | null; hallazgo: HallazgoResponse | null }> {
  const res = await escribir('/hallazgos', 'POST', payload, usuarioId,
    'Servicio de registro no disponible');
  const location = res.headers.get('Location');
  const extractedId = location ? location.split('/').pop() || null : null;
  if (extractedId) {
    const hallazgo = await obtenerHallazgoPorId(extractedId);
    return { id: extractedId, hallazgo };
  }
  return { id: null, hallazgo: null };
}

export async function obtenerHallazgoPorId(id: string): Promise<HallazgoResponse> {
  const res = await fetch(`${API_BASE}/hallazgos/${id}`, { cache: 'no-store' });
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res, 'Hallazgo no encontrado en el servidor'));
  }
  return res.json();
}

export async function modificarHallazgo(
  id: string,
  payload: ModificarHallazgoRequest,
  usuarioId: string
): Promise<HallazgoResponse> {
  const res = await escribir(`/hallazgos/${id}`, 'PATCH', payload, usuarioId,
    'No se pudo sincronizar la modificación');
  return res.json();
}

export async function reclasificarHallazgo(
  id: string,
  payload: ReclasificarHallazgoRequest,
  usuarioId: string
): Promise<HallazgoResponse> {
  const res = await escribir(`/hallazgos/${id}/reclasificacion`, 'POST', payload, usuarioId,
    'No se pudo sincronizar la reclasificación');
  return res.json();
}
