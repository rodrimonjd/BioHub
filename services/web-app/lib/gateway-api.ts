export interface ConsumoConsumidor {
  id: string;
  organizacion: string;
  solicitudes: number;
  rechazadas: number;
  limitePorMinuto: number;
  segundosParaReiniciar: number;
}

export interface ResumenConsumo {
  urlPublica: string;
  consumidores: ConsumoConsumidor[];
}

/** Consumo de la API externa; el gateway exige el permiso api_externa:monitorear. */
export async function obtenerConsumo(usuarioId: string): Promise<ResumenConsumo | null> {
  try {
    const res = await fetch('/api/gateway/consumo', {
      cache: 'no-store',
      headers: { 'X-Usuario-Id': usuarioId },
    });
    return res.ok ? res.json() : null;
  } catch {
    return null;
  }
}

export async function consultarInventario(apiKey: string): Promise<number> {
  try {
    const res = await fetch('/api/externa/inventario?tamano=1', {
      cache: 'no-store',
      headers: { 'X-API-Key': apiKey },
    });
    return res.status;
  } catch {
    return 0;
  }
}
