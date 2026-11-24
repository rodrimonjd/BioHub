'use client';

import React, { useCallback, useEffect, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { BioHubUser } from '@/lib/permissions';
import {
  AccesoUbicacionVigente,
  CambioAccesoUbicacion,
  historialAccesosUbicacion,
  listarAccesosUbicacion,
  otorgarAccesoUbicacion,
  ResumenUsuario,
  revocarAccesoUbicacion,
  usuariosParaUbicacion,
} from '@/lib/autorizacion-api';
import { EspecieRegistro } from '@/lib/taxonomia-api';
import { Aviso, CLASE_INPUT, Encabezado, MensajeAviso } from './campo-ui';

export interface AccesoUbicacionesProps {
  usuario: BioHubUser;
  especies: EspecieRegistro[];
  /** Se llama tras dar o quitar un acceso, por si la cuenta actual cambia lo que ve. */
  onCambio?: () => void;
}

function fechaHora(iso: string): string {
  return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
}

/**
 * La autoridad ambiental (ubicacion:administrar) decide quién ve la ubicación exacta y de qué especies.
 * Cada acceso que da o quita queda en un historial que no se puede modificar.
 */
export function AccesoUbicaciones({ usuario, especies, onCambio }: AccesoUbicacionesProps) {
  const [usuarios, setUsuarios] = useState<ResumenUsuario[]>([]);
  const [accesos, setAccesos] = useState<AccesoUbicacionVigente[]>([]);
  const [historial, setHistorial] = useState<CambioAccesoUbicacion[]>([]);
  const [usuarioId, setUsuarioId] = useState('');
  const [especieId, setEspecieId] = useState('');
  const [aviso, setAviso] = useState<Aviso>(null);
  const [guardando, setGuardando] = useState(false);

  const nombreEspecie = (id: string) => {
    const e = especies.find((x) => x.id === id);
    return e ? `${e.nombreComun ?? e.nombreCientifico} (${e.nombreCientifico})` : id;
  };

  const recargar = useCallback(async () => {
    try {
      const [lista, cambios] = await Promise.all([
        listarAccesosUbicacion(usuario.id),
        historialAccesosUbicacion(usuario.id),
      ]);
      setAccesos(lista);
      setHistorial(cambios);
    } catch (error) {
      setAviso({ tipo: 'err', texto: error instanceof Error ? error.message : 'No se pudieron cargar los accesos.' });
    }
  }, [usuario.id]);

  useEffect(() => {
    usuariosParaUbicacion(usuario.id).then(setUsuarios).catch(() => setUsuarios([]));
    recargar();
  }, [usuario.id, recargar]);

  const handleOtorgar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!usuarioId || !especieId) return;
    setGuardando(true);
    setAviso(null);
    try {
      const acceso = await otorgarAccesoUbicacion(usuario.id, usuarioId, especieId);
      setAviso({
        tipo: 'ok',
        texto: `${acceso.nombreUsuario} ahora ve la ubicación exacta de ${nombreEspecie(acceso.especieId)}.`,
      });
      await recargar();
      onCambio?.();
    } catch (error) {
      setAviso({ tipo: 'err', texto: error instanceof Error ? error.message : 'No se pudo dar el acceso.' });
    } finally {
      setGuardando(false);
    }
  };

  const handleRevocar = async (acceso: AccesoUbicacionVigente) => {
    setAviso(null);
    try {
      await revocarAccesoUbicacion(usuario.id, acceso.usuarioId, acceso.especieId);
      setAviso({
        tipo: 'ok',
        texto: `${acceso.nombreUsuario} ya no ve la ubicación exacta de ${nombreEspecie(acceso.especieId)}.`,
      });
      await recargar();
      onCambio?.();
    } catch (error) {
      setAviso({ tipo: 'err', texto: error instanceof Error ? error.message : 'No se pudo quitar el acceso.' });
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
      <Card className="space-y-4">
        <Encabezado
          titulo="Quién ve la ubicación exacta"
          descripcion="Da acceso a las coordenadas exactas de una especie a una cuenta que no las ve por su rol. Quien tiene el permiso general ya ve todas."
        />
        <MensajeAviso aviso={aviso} />
        <form onSubmit={handleOtorgar} className="grid grid-cols-1 sm:grid-cols-2 gap-3 items-end">
          <Field id="acceso-usuario" label="Cuenta">
            <select
              id="acceso-usuario"
              required
              value={usuarioId}
              onChange={(e) => setUsuarioId(e.target.value)}
              className={CLASE_INPUT}
            >
              <option value="">Elegir...</option>
              {usuarios.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.nombre} ({u.id})
                </option>
              ))}
            </select>
          </Field>
          <Field id="acceso-especie" label="Especie">
            <select
              id="acceso-especie"
              required
              value={especieId}
              onChange={(e) => setEspecieId(e.target.value)}
              className={CLASE_INPUT}
            >
              <option value="">Elegir...</option>
              {especies.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nombreComun ?? e.nombreCientifico} ({e.nombreCientifico}) [{e.estadoConservacion}]
                </option>
              ))}
            </select>
          </Field>
          <Button type="submit" variant="ghost" className="sm:col-span-2 text-xs" disabled={guardando}>
            {guardando ? 'Guardando...' : 'Dar acceso a la ubicación exacta'}
          </Button>
        </form>

        <div className="space-y-2">
          <p className="font-mono text-xs font-semibold uppercase tracking-wider text-moss-500">
            Accesos vigentes ({accesos.length})
          </p>
          <ul className="divide-y divide-line rounded-md border border-line text-xs">
            {accesos.length === 0 && (
              <li className="px-3 py-2 text-ink-muted">Ninguna cuenta tiene accesos por especie.</li>
            )}
            {accesos.map((a) => (
              <li key={`${a.usuarioId}-${a.especieId}`} className="flex flex-wrap items-center justify-between gap-2 px-3 py-2">
                <span className="text-ink">
                  <span className="font-semibold">{a.nombreUsuario}</span> · {nombreEspecie(a.especieId)}
                  <span className="block font-mono text-[11px] text-ink-muted">
                    Desde {fechaHora(a.otorgadoEn)} · por {a.otorgadoPor}
                  </span>
                </span>
                <button
                  type="button"
                  onClick={() => handleRevocar(a)}
                  className="min-h-9 cursor-pointer rounded-md border border-iucn-cr/50 px-3 font-mono text-[11px] text-ink hover:bg-iucn-cr/10"
                >
                  Quitar acceso
                </button>
              </li>
            ))}
          </ul>
        </div>
      </Card>

      <Card className="space-y-4">
        <Encabezado
          titulo="Historial de cambios de acceso"
          descripcion="Cada acceso que se da o se quita queda registrado con quién lo hizo y cuándo. Este historial no se puede modificar ni borrar."
        />
        <ol className="max-h-96 space-y-2 overflow-y-auto">
          {historial.length === 0 && <li className="text-xs text-ink-muted">Todavía no hay cambios registrados.</li>}
          {historial.map((c) => (
            <li key={c.id} className="rounded-md border border-line px-3 py-2 text-xs">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <span
                  className={`rounded px-2 py-0.5 font-mono text-[11px] font-semibold ${
                    c.accion === 'OTORGADO' ? 'bg-iucn-lc/15 text-ink' : 'bg-iucn-cr/15 text-ink'
                  }`}
                >
                  {c.accion === 'OTORGADO' ? 'Acceso dado' : 'Acceso quitado'}
                </span>
                <span className="font-mono text-[11px] text-ink-muted">{fechaHora(c.realizadoEn)}</span>
              </div>
              <p className="mt-1 text-ink">
                {c.nombreUsuario} · {nombreEspecie(c.especieId)}
              </p>
              <p className="font-mono text-[11px] text-ink-muted">Por {c.realizadoPor}</p>
            </li>
          ))}
        </ol>
      </Card>
    </div>
  );
}
