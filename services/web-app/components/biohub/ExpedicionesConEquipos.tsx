'use client';

import React, { useId, useState } from 'react';
import { AnimatePresence, motion, useReducedMotion } from 'motion/react';
import { Card } from '@/components/ui/Card';
import { Equipo, Expedicion } from '@/lib/registro-api';
import { Encabezado } from './campo-ui';

export interface ExpedicionesConEquiposProps {
  /** Cuenta actual: "Mis expediciones" son las que lidera. */
  usuarioId: string;
  expediciones: Expedicion[];
  /** Nombre de una cuenta a partir de su id. */
  nombreDe: (usuarioId: string) => string;
  /** Expedición abierta, o null si todas están cerradas. */
  abierta: string | null;
  onAbrir: (id: string | null) => void;
}

const ESTADO_EQUIPO: Record<string, { texto: string; color: string }> = {
  DISPONIBLE: { texto: 'Disponible', color: 'bg-iucn-lc' },
  EN_USO: { texto: 'En uso', color: 'bg-sun-500' },
  MANTENIMIENTO: { texto: 'En mantenimiento', color: 'bg-iucn-cr' },
};

function fecha(aaaaMmDd: string): string {
  return new Date(`${aaaaMmDd}T12:00:00`).toLocaleDateString('es-CO', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

function periodo(ex: Expedicion): string {
  return ex.fechaFin ? `${fecha(ex.fechaInicio)} a ${fecha(ex.fechaFin)}` : `Desde ${fecha(ex.fechaInicio)}`;
}

/** Los tipos antiguos se guardaron como CAMARA_TRAMPA: se muestran como "Cámara trampa". */
function tipoLegible(tipo: string): string {
  if (tipo !== tipo.toUpperCase() || tipo.length <= 3) return tipo;
  const texto = tipo.replace(/_/g, ' ').toLowerCase().replace('camara', 'cámara');
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

function cuantos(n: number): string {
  return n === 0 ? 'Sin equipos' : n === 1 ? '1 equipo' : `${n} equipos`;
}

/** HU-04: cada expedición con el listado de los equipos que usa. */
export function ExpedicionesConEquipos({
  usuarioId,
  expediciones,
  nombreDe,
  abierta,
  onAbrir,
}: ExpedicionesConEquiposProps) {
  const mias = expediciones.filter((ex) => ex.liderId === usuarioId);
  const [eleccion, setEleccion] = useState<'mias' | 'todas' | null>(null);
  const vista = eleccion ?? (mias.length > 0 ? 'mias' : 'todas');
  // La que se acaba de abrir desde "Asignar equipo" se ve aunque no sea propia
  const lista = [...(vista === 'mias' ? expediciones.filter((ex) => ex.liderId === usuarioId || ex.id === abierta) : expediciones)]
    .sort((a, b) => b.fechaInicio.localeCompare(a.fechaInicio) || a.nombre.localeCompare(b.nombre, 'es'));

  return (
    <Card as="section" className="space-y-4">
      <Encabezado
        titulo="Expediciones y sus equipos"
        descripcion="Abre una expedición para ver cuáles equipos lleva y quién responde por ellos."
      />

      <div role="group" aria-label="Expediciones que se muestran" className="flex flex-wrap gap-2">
        {(
          [
            ['mias', `Mis expediciones (${mias.length})`],
            ['todas', `Todas (${expediciones.length})`],
          ] as const
        ).map(([id, texto]) => (
          <button
            key={id}
            type="button"
            aria-pressed={vista === id}
            onClick={() => setEleccion(id)}
            className={`min-h-11 cursor-pointer rounded-full border px-4 text-xs font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 ${
              vista === id
                ? 'border-forest-700 bg-forest-700 text-white'
                : 'border-line text-ink-muted hover:border-forest-700 hover:text-ink'
            }`}
          >
            {texto}
          </button>
        ))}
      </div>

      {lista.length === 0 ? (
        <p className="py-4 text-center text-sm text-ink-muted">
          {vista === 'mias' && expediciones.length > 0
            ? 'No lideras ninguna expedición todavía.'
            : 'Todavía no hay expediciones registradas.'}
        </p>
      ) : (
        <ul className="space-y-3">
          {lista.map((ex) => (
            <FilaExpedicion
              key={ex.id}
              expedicion={ex}
              nombreDe={nombreDe}
              abierta={abierta === ex.id}
              onAlternar={() => onAbrir(abierta === ex.id ? null : ex.id)}
            />
          ))}
        </ul>
      )}
    </Card>
  );
}

interface FilaExpedicionProps {
  expedicion: Expedicion;
  nombreDe: (usuarioId: string) => string;
  abierta: boolean;
  onAlternar: () => void;
}

function FilaExpedicion({ expedicion, nombreDe, abierta, onAlternar }: FilaExpedicionProps) {
  const idPanel = useId();
  const sinMovimiento = useReducedMotion();
  const equipos = [...(expedicion.equipos ?? [])].sort((a, b) => a.numeroSerie.localeCompare(b.numeroSerie));

  return (
    <li className="overflow-hidden rounded-lg border border-line bg-paper">
      <button
        type="button"
        aria-expanded={abierta}
        aria-controls={idPanel}
        onClick={onAlternar}
        className="flex w-full cursor-pointer items-center justify-between gap-3 px-4 py-3 text-left transition-colors hover:bg-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-sun-500"
      >
        <span className="min-w-0">
          <span className="block font-serif text-base font-semibold text-ink">{expedicion.nombre}</span>
          <span className="block font-mono text-xs text-ink-muted">
            {expedicion.zona} · {periodo(expedicion)} · Líder: {nombreDe(expedicion.liderId)}
          </span>
        </span>
        <span className="flex shrink-0 items-center gap-2 text-xs font-semibold text-ink">
          {cuantos(equipos.length)}
          <svg
            aria-hidden="true"
            viewBox="0 0 16 16"
            className={`h-3.5 w-3.5 fill-none stroke-moss-500 stroke-2 transition-transform duration-200 ${
              abierta ? 'rotate-180' : ''
            }`}
          >
            <path d="M3.5 6l4.5 4.5L12.5 6" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </span>
      </button>

      <div id={idPanel}>
        <AnimatePresence initial={false}>
          {abierta && (
            <motion.div
              initial={sinMovimiento ? false : { height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={sinMovimiento ? undefined : { height: 0, opacity: 0 }}
              transition={{ duration: 0.25, ease: 'easeOut' }}
              className="overflow-hidden"
            >
              {equipos.length === 0 ? (
                <p className="border-t border-line px-4 py-3 text-xs text-ink-muted">
                  Esta expedición no tiene equipos asignados.
                </p>
              ) : (
                <ul
                  aria-label={`Equipos de ${expedicion.nombre}`}
                  className="divide-y divide-line border-t border-line bg-surface"
                >
                  {equipos.map((eq) => (
                    <FilaEquipo key={eq.id} equipo={eq} nombreDe={nombreDe} />
                  ))}
                </ul>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </li>
  );
}

function FilaEquipo({ equipo, nombreDe }: { equipo: Equipo; nombreDe: (usuarioId: string) => string }) {
  const estado = ESTADO_EQUIPO[equipo.estado] ?? { texto: equipo.estado, color: 'bg-ink-muted' };
  return (
    <li className="flex flex-col gap-1 px-4 py-3 text-xs sm:flex-row sm:items-center sm:justify-between sm:gap-4">
      <span className="min-w-0">
        <span className="block font-semibold text-ink">
          {tipoLegible(equipo.tipo)}
          {equipo.modelo ? ` · ${equipo.modelo}` : ''}
        </span>
        <span className="block font-mono text-ink-muted">Serie {equipo.numeroSerie}</span>
      </span>
      <span className="sm:text-right">
        <span className="flex items-center gap-1.5 text-ink sm:justify-end">
          <span aria-hidden="true" className={`h-2 w-2 rounded-full ${estado.color}`} />
          {estado.texto}
        </span>
        <span className="block text-ink-muted">
          {equipo.responsableId ? `Responsable: ${nombreDe(equipo.responsableId)}` : 'Sin responsable'}
        </span>
      </span>
    </li>
  );
}
