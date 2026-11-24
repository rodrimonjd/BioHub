const API_BASE = '/api/taxonomia';

export type TipoDato = 'TEXTO' | 'NUMERICO' | 'LISTA';

export interface AtributoTaxonomico {
  id: string;
  nombre: string;
  tipoDato: TipoDato;
  unidad: string | null;
  opciones: string[] | null;
  obligatorio: boolean;
}

export interface ValorAtributo {
  atributo: AtributoTaxonomico | null;
  valor: string | null;
}

export interface FichaTaxonomica {
  id: string;
  especieId: string;
  version: number;
  activa: boolean;
  atributos: ValorAtributo[];
  creadoPor: string;
  creadoEn: string;
  actualizadoEn: string;
}

export interface EspecieRegistro {
  id: string;
  nombreCientifico: string;
  nombreComun: string | null;
  estadoConservacion: string;
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

export async function cargarInventario(): Promise<{
  especies: EspecieRegistro[];
  fichas: FichaTaxonomica[];
} | null> {
  try {
    const [resEspecies, resFichas] = await Promise.all([
      fetch('/api/registro/especies', { cache: 'no-store' }),
      fetch(`${API_BASE}/fichas`, { cache: 'no-store' }),
    ]);
    if (!resEspecies.ok || !resFichas.ok) return null;
    return { especies: await resEspecies.json(), fichas: await resFichas.json() };
  } catch {
    return null;
  }
}

// Las escrituras van con X-Usuario-Id: taxonomía exige el permiso ficha:editar y usa ese id como autor.
async function asegurarAtributo(nombre: string, autor: string): Promise<void> {
  const resCatalogo = await fetch(`${API_BASE}/atributos`, { cache: 'no-store' });
  if (resCatalogo.ok) {
    const catalogo: AtributoTaxonomico[] = await resCatalogo.json();
    if (catalogo.some((a) => a.nombre.toLowerCase() === nombre.toLowerCase())) return;
  }
  const res = await fetch(`${API_BASE}/atributos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-Usuario-Id': autor },
    body: JSON.stringify({ nombre, tipoDato: 'TEXTO' }),
  });
  if (!res.ok && res.status !== 409) {
    throw new Error(await mensajeDeError(res, 'No se pudo crear el atributo en el catálogo'));
  }
}

export async function guardarAtributoEnFicha(params: {
  especieId: string;
  ficha: FichaTaxonomica | null;
  nombre: string;
  valor: string;
  autor: string;
}): Promise<FichaTaxonomica> {
  const { especieId, ficha, nombre, valor, autor } = params;
  await asegurarAtributo(nombre, autor);

  let res: Response;
  if (!ficha) {
    res = await fetch(`${API_BASE}/fichas`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-Usuario-Id': autor },
      body: JSON.stringify({ especieId, autor, atributos: [{ nombre, valor }] }),
    });
  } else {
    const yaLoTiene = ficha.atributos.some(
      (a) => a.atributo?.nombre.toLowerCase() === nombre.toLowerCase()
    );
    res = yaLoTiene
      ? await fetch(`${API_BASE}/fichas/${ficha.id}/atributos/${encodeURIComponent(nombre)}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', 'X-Usuario-Id': autor },
          body: JSON.stringify({ valor, autor }),
        })
      : await fetch(`${API_BASE}/fichas/${ficha.id}/atributos`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'X-Usuario-Id': autor },
          body: JSON.stringify({ nombre, valor, autor }),
        });
  }
  if (!res.ok) {
    throw new Error(await mensajeDeError(res, 'No se pudo guardar el atributo en la ficha'));
  }
  return res.json();
}
