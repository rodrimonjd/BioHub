'use client';

import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { useNotificar } from '@/components/ui/Notificaciones';
import { avisoDeError } from '@/lib/avisos';
import { cambiarLimite, consultarInventario, obtenerConsumo, ResumenConsumo } from '@/lib/gateway-api';

const INTERVALO_CONSULTA_MS = 2000;

const CANTIDADES = [1, 10, 70];

function claseBarra(solicitudes: number, rechazadas: number, limite: number): string {
  if (rechazadas > 0) return 'bg-iucn-cr';
  if (solicitudes / limite > 0.8) return 'bg-sun-500';
  return 'bg-moss-500';
}

function estado(solicitudes: number, rechazadas: number, limite: number): string {
  if (rechazadas > 0) return `Límite alcanzado · ${rechazadas} rechazada(s) con 429`;
  if (solicitudes / limite > 0.8) return 'Cerca del límite por minuto (>80%)';
  return 'Dentro del límite permitido';
}

export interface ApiUsagePanelProps {
  usuarioId: string;
  puedeConfigurar: boolean;
}

export function ApiUsagePanel({ usuarioId, puedeConfigurar }: ApiUsagePanelProps) {
  const [resumen, setResumen] = useState<ResumenConsumo | null>(null);
  const [conectado, setConectado] = useState<boolean>(true);
  const [apiKey, setApiKey] = useState<string>('');
  const [cantidad, setCantidad] = useState<number>(10);
  const [enviando, setEnviando] = useState<boolean>(false);
  const [resultado, setResultado] = useState<string | null>(null);
  const [nuevoLimite, setNuevoLimite] = useState<string>('');
  const [guardandoLimite, setGuardandoLimite] = useState<boolean>(false);
  const notificar = useNotificar();

  const recargar = async () => {
    const datos = await obtenerConsumo();
    setConectado(datos !== null);
    if (datos) setResumen(datos);
  };

  useEffect(() => {
    const consultar = async () => {
      const datos = await obtenerConsumo();
      setConectado(datos !== null);
      if (datos) setResumen(datos);
    };
    consultar();
    const temporizador = setInterval(consultar, INTERVALO_CONSULTA_MS);
    return () => clearInterval(temporizador);
  }, [usuarioId]);

  const handleEnviar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!apiKey.trim()) return;
    setEnviando(true);
    setResultado(null);
    const conteo: Record<number, number> = {};
    for (let i = 0; i < cantidad; i++) {
      const codigo = await consultarInventario(apiKey.trim());
      conteo[codigo] = (conteo[codigo] ?? 0) + 1;
    }
    setResultado(
      Object.entries(conteo)
        .map(([codigo, veces]) => `${codigo === '0' ? 'sin conexión' : `HTTP ${codigo}`}: ${veces}`)
        .join(' · ') +
        (conteo[401]
          ? ' — La API key no es válida: debe escribirse exacta, en minúsculas y sin comillas.'
          : '')
    );
    setEnviando(false);
    await recargar();
  };

  const handleCambiarLimite = async (e: React.FormEvent) => {
    e.preventDefault();
    setGuardandoLimite(true);
    try {
      const actualizado = await cambiarLimite(Number(nuevoLimite));
      setResumen(actualizado);
      setNuevoLimite('');
      notificar({
        tipo: 'ok',
        titulo: 'Límite actualizado',
        detalle: `Cada consumidor puede hacer ${actualizado.consumidores[0]?.limitePorMinuto} solicitudes por minuto.`,
      });
    } catch (error) {
      notificar(
        avisoDeError(error, {
          titulo: 'No se cambió el límite',
          sinConexion: 'El límite sigue igual',
          accion: 'cambiar el límite de la API',
        })
      );
    } finally {
      setGuardandoLimite(false);
    }
  };

  const limite = resumen?.consumidores[0]?.limitePorMinuto ?? 60;
  const limiteMaximo = resumen?.limiteMaximo ?? 60;
  const reinicio = resumen?.consumidores[0]?.segundosParaReiniciar;

  return (
    <Card as="section" aria-labelledby="api-usage-heading" className="space-y-4">
      <div className="flex flex-wrap items-start justify-between gap-2 border-b border-line pb-3">
        <div>
          <span className="font-mono text-xs uppercase tracking-wider text-ink-muted">
            Punto de entrada controlado · Plataforma abierta
          </span>
          <h3 id="api-usage-heading" className="mt-1 font-serif text-lg font-semibold text-ink">
            Control de peticiones por consumidor externo
          </h3>
        </div>
        <span className="font-mono text-xs text-ink-muted">Tope: {limite} req/min</span>
      </div>

      {!conectado && (
        <p role="alert" className="rounded-md border border-iucn-cr bg-iucn-cr/10 px-3 py-2 font-mono text-xs text-ink">
          No hay conexión con el API Gateway.
        </p>
      )}

      {resumen && (
        <div className="space-y-1 text-xs">
          <p className="font-mono text-ink-muted">
            Acceso público estable: {resumen.urlPublica}/api/v1/inventario
          </p>
          {reinicio !== undefined && (
            <p className="font-mono text-ink-muted">El contador vuelve a 0 en {reinicio} s</p>
          )}
        </div>
      )}

      <ul className="space-y-4">
        {resumen?.consumidores.map((c) => (
          <li key={c.id} className="space-y-1.5">
            <div className="flex flex-wrap items-baseline justify-between gap-2">
              <span className="text-xs font-medium text-ink">{c.organizacion}</span>
              <span className="font-mono text-sm font-bold text-ink">
                {c.solicitudes} / {c.limitePorMinuto} req/min
              </span>
            </div>
            <div
              role="progressbar"
              aria-valuenow={c.solicitudes}
              aria-valuemin={0}
              aria-valuemax={c.limitePorMinuto}
              aria-label={`Peticiones por minuto de ${c.organizacion}`}
              className="h-3 w-full overflow-hidden rounded-md border border-line bg-paper"
            >
              <div
                className={`h-full transition-all duration-150 ${claseBarra(c.solicitudes, c.rechazadas, c.limitePorMinuto)}`}
                style={{ width: `${Math.min(100, Math.round((c.solicitudes / c.limitePorMinuto) * 100))}%` }}
              />
            </div>
            <p className="font-mono text-xs text-ink-muted">
              {estado(c.solicitudes, c.rechazadas, c.limitePorMinuto)}
            </p>
          </li>
        ))}
      </ul>

      {puedeConfigurar && (
        <form onSubmit={handleCambiarLimite} className="space-y-3 border-t border-line pt-4">
          <p className="font-mono text-xs font-semibold uppercase tracking-wider text-moss-500">
            Cambiar el límite por minuto
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 items-end">
            <Field id="gw-limite" label={`Solicitudes por minuto (1 a ${limiteMaximo})`} className="sm:col-span-2">
              <input
                id="gw-limite"
                type="number"
                min={1}
                max={limiteMaximo}
                step={1}
                required
                value={nuevoLimite}
                onChange={(e) => setNuevoLimite(e.target.value)}
                placeholder={String(limite)}
                className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
              />
            </Field>
            <Button type="submit" variant="ghost" className="w-full text-xs" disabled={guardandoLimite || !nuevoLimite}>
              {guardandoLimite ? 'Guardando...' : 'Guardar límite'}
            </Button>
          </div>
        </form>
      )}

      <form onSubmit={handleEnviar} className="space-y-3 border-t border-line pt-4">
        <p className="font-mono text-xs font-semibold uppercase tracking-wider text-moss-500">
          Probar la API pública
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 items-end">
          <Field id="gw-api-key" label="API key del consumidor" className="sm:col-span-2">
            <input
              id="gw-api-key"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="unal-demo-2026"
              autoComplete="off"
              autoCapitalize="none"
              autoCorrect="off"
              spellCheck={false}
              className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
            />
          </Field>
          <Field id="gw-cantidad" label="Solicitudes">
            <select
              id="gw-cantidad"
              value={cantidad}
              onChange={(e) => setCantidad(Number(e.target.value))}
              className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
            >
              {CANTIDADES.map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
          </Field>
        </div>
        <Button type="submit" variant="ghost" className="w-full text-xs" disabled={enviando || !apiKey.trim()}>
          {enviando ? 'Enviando...' : 'Enviar solicitudes al gateway'}
        </Button>
        {resultado && (
          <p role="status" className="rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink">
            Resultado: {resultado}
          </p>
        )}
      </form>
    </Card>
  );
}
