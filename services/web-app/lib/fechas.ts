/** Título del día para agrupar registros: "Hoy", "Ayer", "22 de noviembre" (con el año si no es el actual). */
export function tituloDeDia(iso: string): string {
  const fecha = new Date(iso);
  if (Number.isNaN(fecha.getTime())) return 'Sin fecha';
  const hoy = new Date();
  const ayer = new Date(hoy);
  ayer.setDate(hoy.getDate() - 1);
  if (fecha.toDateString() === hoy.toDateString()) return 'Hoy';
  if (fecha.toDateString() === ayer.toDateString()) return 'Ayer';
  return fecha.toLocaleDateString('es-CO', {
    day: 'numeric',
    month: 'long',
    year: fecha.getFullYear() === hoy.getFullYear() ? undefined : 'numeric',
  });
}
