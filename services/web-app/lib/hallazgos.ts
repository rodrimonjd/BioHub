import { HallazgoResponse, SeedSpecies } from '@/lib/registro-api';

/** Nombre común de la especie del hallazgo según el catálogo (si no está, el científico que trae el hallazgo). */
export function nombreComunHallazgo(h: HallazgoResponse, catalogo: SeedSpecies[]): string {
  return (
    catalogo.find((s) => s.id === h.especieId)?.commonName ??
    catalogo.find((s) => s.scientificName.toLowerCase() === h.especie.toLowerCase())?.commonName ??
    h.especie
  );
}

function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
}

/**
 * Busca por nombre: especie (común o científico), departamento, región, investigador o descripción.
 * El código corto del hallazgo también sirve. Sin tildes ni mayúsculas.
 */
export function coincideHallazgo(h: HallazgoResponse, texto: string, catalogo: SeedSpecies[]): boolean {
  const consulta = normalizar(texto.trim());
  if (!consulta) return true;
  const campos = [
    h.especie,
    nombreComunHallazgo(h, catalogo),
    h.departamento,
    h.region ?? '',
    h.investigadorId,
    h.descripcion,
    h.id,
  ];
  return consulta
    .split(/\s+/)
    .every((palabra) => campos.some((c) => normalizar(c).includes(palabra)));
}

/** Día (AAAA-MM-DD) en la hora local del navegador en que se registró el hallazgo. */
export function diaLocal(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? '' : d.toLocaleDateString('en-CA');
}

/**
 * Si el hallazgo cae entre dos fechas (AAAA-MM-DD), ambas incluidas. Una fecha vacía no limita.
 */
export function enRangoDeFechas(h: HallazgoResponse, desde: string, hasta: string): boolean {
  const dia = diaLocal(h.fecha);
  if (!dia) return !desde && !hasta;
  return (!desde || dia >= desde) && (!hasta || dia <= hasta);
}
