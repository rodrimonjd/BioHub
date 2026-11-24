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
