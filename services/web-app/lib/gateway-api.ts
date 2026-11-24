import { pedir } from '@/lib/pedir';

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
  limiteMaximo: number;
  consumidores: ConsumoConsumidor[];
}

/** Consumo de la API externa; el gateway exige el permiso api_externa:monitorear. */
export async function obtenerConsumo(): Promise<ResumenConsumo | null> {
  try {
    const res = await pedir('/api/gateway/consumo', { cache: 'no-store' });
    return res.ok ? res.json() : null;
  } catch {
    return null;
  }
}

export async function cambiarLimite(limitePorMinuto: number): Promise<ResumenConsumo> {
  let res: Response;
  try {
    res = await pedir('/api/gateway/limite', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ limitePorMinuto }),
    });
  } catch {
    throw new Error('No hay conexión con el API Gateway.');
  }
  if (res.status === 403) throw new Error('Tu cuenta no tiene permiso para cambiar el límite.');
  if (res.status === 400) throw new Error('El límite no está dentro del rango permitido.');
  if (!res.ok) throw new Error('No se pudo cambiar el límite.');
  return res.json();
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
