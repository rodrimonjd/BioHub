'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { BioHubUser, PERMISOS, tienePermiso } from '@/lib/permissions';
import { HallazgoResponse, listarHallazgos } from '@/lib/registro-api';
import { EspecieRegistro, FichaTaxonomica } from '@/lib/taxonomia-api';
import {
  CambioHistorial,
  ErrorHistorial,
  Historial,
  obtenerHistorial,
  TipoCambio,
  TipoRegistroHistorial,
} from '@/lib/historial-api';

export interface HistorialRegistrosProps {
  usuario: BioHubUser;
  /** Fichas y especies del inventario, para elegir una ficha por el nombre de su especie. */
  fichas: FichaTaxonomica[];
  especies: EspecieRegistro[];
  /** Hallazgo que se acaba de modificar en la estación de campo: se selecciona y se recarga. */
  hallazgoActivo?: string | null;
  /** Cambia cada vez que hay que volver a pedir el historial. */
  recarga?: number;
}

const CLASE_SELECT =
  'min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink focus:outline-none focus:ring-2 focus:ring-moss-500';

const ETIQUETA_CAMBIO: Record<TipoCambio, string> = {
  CREACION: 'Creación',
  MODIFICACION: 'Modificación',
  RECLASIFICACION: 'Reclasificación',
  DESACTIVACION: 'Desactivación',
};

const ESTILO_CAMBIO: Record<TipoCambio, string> = {
  CREACION: 'border-iucn-lc text-ink bg-iucn-lc/10',
  MODIFICACION: 'border-line text-ink bg-surface',
  RECLASIFICACION: 'border-sun-500 text-ink bg-sun-500/10',
  DESACTIVACION: 'border-iucn-cr text-ink bg-iucn-cr/10',
};

const FORMATO_FECHA = new Intl.DateTimeFormat('es-CO', {
  dateStyle: 'medium',
  timeStyle: 'short',
});

function fecha(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : FORMATO_FECHA.format(d);
}

function vacio(valor: string | null): string {
  return valor === null || valor === '' ? '(vacío)' : valor;
}

/**
 * Cronología de cambios de un hallazgo o una ficha taxonómica, leída de servicio-historial
 * (registro inmutable alimentado por eventos). Requiere el permiso historial:ver.
 */
export function HistorialRegistros({
  usuario,
  fichas,
  especies,
  hallazgoActivo,
  recarga = 0,
}: HistorialRegistrosProps) {
  const puedeVer = tienePermiso(usuario, PERMISOS.VER_HISTORIAL);

  const [tipo, setTipo] = useState<TipoRegistroHistorial>('hallazgos');
  const [hallazgos, setHallazgos] = useState<HallazgoResponse[]>([]);
  const [registroId, setRegistroId] = useState<string>('');
  const [filtro, setFiltro] = useState<'TODOS' | TipoCambio>('TODOS');
  const [recientesPrimero, setRecientesPrimero] = useState<boolean>(false);

  const [historial, setHistorial] = useState<Historial | null>(null);
  const [cargando, setCargando] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Hallazgos guardados en servicio-registro
  useEffect(() => {
    if (!puedeVer) return;
    listarHallazgos().then(({ data }) => {
      if (data) setHallazgos(data);
    });
  }, [puedeVer, recarga]);

  // Al modificar un hallazgo en la estación de campo se muestra su historial
  useEffect(() => {
    if (hallazgoActivo) {
      setTipo('hallazgos');
      setRegistroId(hallazgoActivo);
    }
  }, [hallazgoActivo, recarga]);

  const opciones = useMemo(() => {
    if (tipo === 'hallazgos') {
      return hallazgos.map((h) => ({
        id: h.id,
        etiqueta: `${h.especie} · ${h.departamento} · ${fecha(h.fecha)} (#${h.id.slice(0, 8)})`,
      }));
    }
    return fichas.map((f) => {
      const especie = especies.find((e) => e.id === f.especieId);
      const nombre = especie ? especie.nombreComun ?? especie.nombreCientifico : f.especieId.slice(0, 8);
      return {
        id: f.id,
        etiqueta: `${nombre} · ficha v${f.version}${f.activa ? '' : ' (desactivada)'}`,
      };
    });
  }, [tipo, hallazgos, fichas, especies]);

  // Sin registro elegido se toma el primero de la lista
  useEffect(() => {
    if (!registroId && opciones.length > 0) setRegistroId(opciones[0].id);
  }, [opciones, registroId]);

  useEffect(() => {
    if (!puedeVer || !registroId) {
      setHistorial(null);
      return;
    }
    let vigente = true;
    setCargando(true);
    setError(null);
    obtenerHistorial(tipo, registroId, usuario.id)
      .then((h) => vigente && setHistorial(h))
      .catch((e) => {
        if (!vigente) return;
        setHistorial(null);
        setError(e instanceof ErrorHistorial ? e.message : 'No se pudo cargar el historial.');
      })
      .finally(() => vigente && setCargando(false));
    return () => {
      vigente = false;
    };
  }, [puedeVer, tipo, registroId, usuario.id, recarga]);

  const cambios = useMemo(() => {
    const lista = (historial?.cambios ?? []).filter(
      (c) => filtro === 'TODOS' || c.tipoCambio === filtro
    );
    return recientesPrimero ? [...lista].reverse() : lista;
  }, [historial, filtro, recientesPrimero]);

  return (
    <Card as="section" aria-labelledby="timeline-heading" className="space-y-5">
      <div className="flex flex-wrap items-baseline justify-between gap-2 border-b border-line pb-4">
        <div>
          <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
            Trazabilidad inmutable · Nunca se borra un evento anterior
          </span>
          <h3 id="timeline-heading" className="mt-1 font-serif text-xl font-semibold text-ink">
            Historial de cambios
          </h3>
        </div>
        {historial && (
          <span className="font-mono text-xs text-ink-muted">
            Versión actual: v{historial.versionActual}
          </span>
        )}
      </div>

      {!puedeVer ? (
        <p className="rounded-md border border-line bg-paper px-3 py-3 text-xs text-ink-muted">
          Tu cuenta no tiene el permiso para consultar el historial (historial:ver).
        </p>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Field id="hist-tipo" label="Qué ver">
              <select
                id="hist-tipo"
                value={tipo}
                onChange={(e) => {
                  setTipo(e.target.value as TipoRegistroHistorial);
                  setRegistroId('');
                }}
                className={CLASE_SELECT}
              >
                <option value="hallazgos">Hallazgos</option>
                <option value="fichas">Fichas taxonómicas</option>
              </select>
            </Field>
            <Field id="hist-cambio" label="Tipo de cambio">
              <select
                id="hist-cambio"
                value={filtro}
                onChange={(e) => setFiltro(e.target.value as 'TODOS' | TipoCambio)}
                className={CLASE_SELECT}
              >
                <option value="TODOS">Todos los cambios</option>
                {(Object.keys(ETIQUETA_CAMBIO) as TipoCambio[]).map((t) => (
                  <option key={t} value={t}>
                    {ETIQUETA_CAMBIO[t]}
                  </option>
                ))}
              </select>
            </Field>
            <Field
              id="hist-registro"
              label={tipo === 'hallazgos' ? 'Hallazgo' : 'Ficha taxonómica'}
              className="sm:col-span-2"
            >
              <select
                id="hist-registro"
                value={registroId}
                onChange={(e) => setRegistroId(e.target.value)}
                disabled={opciones.length === 0}
                className={CLASE_SELECT}
              >
                {opciones.length === 0 && <option value="">No hay registros guardados</option>}
                {opciones.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.etiqueta}
                  </option>
                ))}
              </select>
            </Field>
          </div>

          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="font-mono text-xs text-ink-muted" aria-live="polite">
              {cargando
                ? 'Cargando historial…'
                : historial
                  ? `${cambios.length} de ${historial.cambios.length} cambio(s) · ${historial.totalModificaciones} modificación(es)`
                  : ''}
            </p>
            <label className="inline-flex items-center gap-2 text-xs text-ink-muted cursor-pointer">
              <input
                type="checkbox"
                checked={recientesPrimero}
                onChange={(e) => setRecientesPrimero(e.target.checked)}
                className="h-4 w-4 accent-moss-500"
              />
              Más recientes primero
            </label>
          </div>

          {error && (
            <p
              role="alert"
              className="rounded-md border border-iucn-cr bg-iucn-cr/10 px-3 py-2 font-mono text-xs text-ink"
            >
              {error}
            </p>
          )}

          {historial && !historial.completo && (
            <p className="rounded-md border border-sun-500 bg-sun-500/10 px-3 py-2 font-mono text-xs text-ink">
              Faltan versiones en el historial: {historial.versionesFaltantes.map((v) => `v${v}`).join(', ')}
            </p>
          )}

          {historial && cambios.length === 0 && (
            <p className="text-xs text-ink-muted">Ningún cambio de este tipo en el registro.</p>
          )}

          {cambios.length > 0 && (
            <ol className="relative border-l border-line ml-3 space-y-5">
              {cambios.map((c) => (
                <EventoHistorial key={c.version} cambio={c} />
              ))}
            </ol>
          )}
        </>
      )}
    </Card>
  );
}

function EventoHistorial({ cambio: c }: { cambio: CambioHistorial }) {
  return (
    <li className="ml-6">
      <span
        className="absolute -left-3 mt-1.5 flex h-6 w-6 items-center justify-center rounded-full border border-forest-700 bg-paper font-mono text-[10px] font-bold text-ink"
        aria-hidden="true"
      >
        v{c.version}
      </span>

      <div className="rounded-lg border border-line bg-paper p-4 space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line pb-2">
          <span
            className={`rounded border px-2 py-0.5 font-mono text-[11px] font-semibold ${ESTILO_CAMBIO[c.tipoCambio]}`}
          >
            {ETIQUETA_CAMBIO[c.tipoCambio]}
          </span>
          <time className="font-mono text-xs text-ink-muted" dateTime={c.ocurrioEn}>
            {fecha(c.ocurrioEn)}
          </time>
        </div>

        {c.tipoCambio === 'CREACION' ? (
          <p className="text-sm text-ink">Registro creado.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div className="rounded-md border border-line bg-surface p-3">
              <p className="font-mono text-[11px] uppercase tracking-wider text-ink-muted">
                {c.tipoCambio === 'RECLASIFICACION' ? 'Qué se creía' : `${c.campo} · antes`}
              </p>
              <p
                className={`mt-1 text-sm text-ink ${c.tipoCambio === 'RECLASIFICACION' ? 'font-serif italic' : ''}`}
              >
                {vacio(c.valorAnterior)}
              </p>
            </div>
            <div className="rounded-md border border-moss-500/40 bg-surface p-3">
              <p className="font-mono text-[11px] uppercase tracking-wider text-moss-500 font-semibold">
                {c.tipoCambio === 'RECLASIFICACION' ? 'Qué se sabe ahora' : `${c.campo} · después`}
              </p>
              <p
                className={`mt-1 text-sm font-medium text-ink ${c.tipoCambio === 'RECLASIFICACION' ? 'font-serif italic' : ''}`}
              >
                {vacio(c.valorNuevo)}
              </p>
            </div>
          </div>
        )}

        <div className="pt-1 text-xs text-ink-muted space-y-1">
          <p>
            <strong className="text-ink font-medium">Autor:</strong>{' '}
            <span className="font-mono text-ink">{c.autor}</span>
            {c.autorizador && (
              <>
                {' · '}
                <strong className="text-ink font-medium">Autorizó:</strong>{' '}
                <span className="font-mono text-ink">{c.autorizador}</span>
              </>
            )}
          </p>
          {c.motivo && (
            <p>
              <strong className="text-ink font-medium">Motivo:</strong> {c.motivo}
            </p>
          )}
        </div>
      </div>
    </li>
  );
}
