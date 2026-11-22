import { ConservationStatus } from '@/components/ui/ConservationBadge';

export interface SeedSpecies {
  id: string;
  scientificName: string;
  commonName: string;
  status: ConservationStatus;
}

/**
 * Especies exactas de V3__semilla__especies.sql en servicio-registro:
 * - 11111111-1111-1111-1111-111111111111 -> Crocodylus intermedius (CR)
 * - 22222222-2222-2222-2222-222222222222 -> Panthera onca (NT)
 * - 33333333-3333-3333-3333-333333333333 -> Ara macao (LC)
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

// DTOs exactos de co.edu.unbosque.biohub.registro_service.avistamiento.*
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
}

// DTOs exactos de co.edu.unbosque.biohub.registro_service.evidencia.*
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

// DTOs exactos de co.edu.unbosque.biohub.registro_service.hallazgo.*
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
  evidencias: EvidenciaRequest[];
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
  creadoEn: string;
  evidencias: EvidenciaResponse[];
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
    if (body?.error) return `${body.error} (HTTP ${res.status})`;
  } catch {
    // ignore JSON parse error
  }
  return `${fallback} (HTTP ${res.status})`;
}

// HU-13: GET /avistamientos
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

// HU-13: GET /avistamientos/{id}
export async function obtenerAvistamientoPorId(id: string): Promise<AvistamientoResponse> {
  const res = await fetch(`${API_BASE}/avistamientos/${id}`, { cache: 'no-store' });
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res, 'Avistamiento no encontrado'));
  }
  return res.json();
}

// HU-13: POST /avistamientos
export async function registrarAvistamiento(
  payload: AvistamientoRequest
): Promise<AvistamientoResponse> {
  const res = await fetch(`${API_BASE}/avistamientos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res, 'No se pudo registrar el avistamiento'));
  }
  return res.json();
}

// HU-01 & HU-03: POST /hallazgos (Devuelve 201 Created con header Location: /hallazgos/{uuid})
export async function registrarHallazgo(
  payload: HallazgoRequest
): Promise<{ id: string | null; hallazgo: HallazgoResponse | null }> {
  const res = await fetch(`${API_BASE}/hallazgos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res, 'Error de validación al registrar el hallazgo'));
  }
  const location = res.headers.get('Location');
  const extractedId = location ? location.split('/').pop() || null : null;
  if (extractedId) {
    const hallazgo = await obtenerHallazgoPorId(extractedId);
    return { id: extractedId, hallazgo };
  }
  return { id: null, hallazgo: null };
}

// HU-01 / HU-05: GET /hallazgos/{id}
export async function obtenerHallazgoPorId(id: string): Promise<HallazgoResponse> {
  const res = await fetch(`${API_BASE}/hallazgos/${id}`, { cache: 'no-store' });
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res, 'Hallazgo no encontrado'));
  }
  return res.json();
}

// HU-19.1 & HU-10: PATCH /hallazgos/{id}
export async function modificarHallazgo(
  id: string,
  payload: ModificarHallazgoRequest
): Promise<HallazgoResponse> {
  const res = await fetch(`${API_BASE}/hallazgos/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res, 'No se pudo modificar el hallazgo'));
  }
  return res.json();
}

// HU-19.1 & HU-10: POST /hallazgos/{id}/reclasificacion
export async function reclasificarHallazgo(
  id: string,
  payload: ReclasificarHallazgoRequest
): Promise<HallazgoResponse> {
  const res = await fetch(`${API_BASE}/hallazgos/${id}/reclasificacion`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(await parseErrorMessage(res, 'No se pudo reclasificar el hallazgo'));
  }
  return res.json();
}

