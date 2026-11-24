'use client';

import React, { useMemo, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { ConservationBadge } from '@/components/ui/ConservationBadge';
import { Expedicion, HallazgoResponse, SeedSpecies } from '@/lib/registro-api';
import { coincideHallazgo, nombreComunHallazgo } from '@/lib/hallazgos';
import { FichaTaxonomica } from '@/lib/taxonomia-api';

export interface ListaHallazgosProps {
  hallazgos: HallazgoResponse[];
  catalogo: SeedSpecies[];
  /** Fichas de servicio-taxonomia, para mostrar la ficha asociada a cada hallazgo. */
  fichas: FichaTaxonomica[];
  /** Expediciones de servicio-registro, para mostrar en cuál se hizo cada hallazgo. */
  expediciones: Expedicion[];
  /** Con ubicacion:ver_exacta se muestran las coordenadas; si no, solo departamento y región. */
  puedeVerExacta: boolean;
  /** Si la cuenta puede modificar hallazgos: lleva el hallazgo a la pestaña de reclasificación. */
  onModificar?: (id: string) => void;
}

const POR_PAGINA = 6;

const ETIQUETA_EVIDENCIA: Record<string, string> = {
  TEMPERATURA: 'Temperatura',
  ADN: 'Muestra ADN',
  AUDIO: 'Audio',
};

function fecha(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime())
    ? iso
    : d.toLocaleDateString('es-CO', { day: 'numeric', month: 'short', year: 'numeric' });
}

/** Hallazgos guardados en servicio-registro, con búsqueda por nombre y detalle desplegable. */
export function ListaHallazgos({
  hallazgos,
  catalogo,
  fichas,
  expediciones,
  puedeVerExacta,
  onModificar,
}: ListaHallazgosProps) {
  const [busqueda, setBusqueda] = useState<string>('');
  const [visibles, setVisibles] = useState<number>(POR_PAGINA);
  const [abierto, setAbierto] = useState<string | null>(null);

  const filtrados = useMemo(
    () => hallazgos.filter((h) => coincideHallazgo(h, busqueda, catalogo)),
    [hallazgos, busqueda, catalogo]
  );

  return (
    <Card as="section" aria-labelledby="lista-hallazgos-heading" className="space-y-4">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-3 border-b border-line pb-4">
        <div>
          <span className="font-mono text-xs uppercase tracking-wider text-ink-muted">
            Servicio de registro
          </span>
          <h3 id="lista-hallazgos-heading" className="mt-1 font-serif text-2xl font-semibold text-ink">
            Hallazgos registrados ({filtrados.length})
          </h3>
        </div>
        <input
          type="search"
          value={busqueda}
          onChange={(e) => {
            setBusqueda(e.target.value);
            setVisibles(POR_PAGINA);
          }}
          placeholder="Buscar por especie, departamento, investigador..."
          aria-label="Buscar hallazgos por nombre"
          className="min-h-11 w-full md:w-80 rounded-md border border-line bg-paper px-3.5 py-2 text-xs text-ink focus:outline-none focus:ring-2 focus:ring-moss-500"
        />
      </div>

      {filtrados.length === 0 ? (
        <p className="py-6 text-center text-sm text-ink-muted">
          {hallazgos.length === 0
            ? 'Todavía no hay hallazgos registrados.'
            : `Ningún hallazgo coincide con "${busqueda}".`}
        </p>
      ) : (
        <ul className="space-y-3">
          {filtrados.slice(0, visibles).map((h) => {
            const especie = catalogo.find((s) => s.id === h.especieId);
            const comun = nombreComunHallazgo(h, catalogo);
            const expandido = abierto === h.id;
            return (
              <li
                key={h.id}
                className={`rounded-lg border p-4 space-y-3 ${
                  h.pendingSync ? 'border-sun-500 bg-sun-500/5' : 'border-line bg-paper'
                }`}
              >
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="font-serif text-base font-semibold text-ink">{comun}</p>
                    <p className="font-serif text-sm italic text-ink-muted">{h.especie}</p>
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    {especie && <ConservationBadge status={especie.status} />}
                    <span className="font-mono text-xs text-ink-muted">v{h.versionActual}</span>
                  </div>
                </div>

                <p className="font-mono text-xs text-ink-muted">
                  {h.departamento}
                  {h.region ? ` · ${h.region}` : ''} · {fecha(h.fecha)} · {h.investigadorId} · #
                  {h.id.slice(0, 8)}
                  {h.pendingSync ? ' · pendiente de sincronizar' : ''}
                </p>

                {expandido && (
                  <div className="space-y-3 border-t border-line pt-3 text-xs">
                    <p className="text-sm text-ink">{h.descripcion}</p>
                    <p className="font-mono text-ink-muted">
                      {(() => {
                        if (!h.expedicionId) return 'Sin expedición asociada';
                        const ex = expediciones.find((e) => e.id === h.expedicionId);
                        return ex
                          ? `Expedición: ${ex.nombre} · ${ex.zona}`
                          : `Expedición #${h.expedicionId.slice(0, 8)}`;
                      })()}
                    </p>
                    <p className="font-mono text-ink-muted">
                      {(() => {
                        if (!h.fichaId) return 'Sin ficha taxonómica asociada';
                        const ficha = fichas.find((f) => f.id === h.fichaId);
                        return ficha
                          ? `Ficha taxonómica v${ficha.version} · ${ficha.atributos
                              .filter((a) => a.atributo && a.valor)
                              .map((a) => `${a.atributo!.nombre}: ${a.valor}`)
                              .join(' · ') || 'sin atributos'}`
                          : `Ficha taxonómica #${h.fichaId.slice(0, 8)}`;
                      })()}
                    </p>
                    <p className="font-mono text-ink-muted">
                      {puedeVerExacta
                        ? h.latitud !== null && h.longitud !== null
                          ? `Coordenadas: ${h.latitud}, ${h.longitud}`
                          : 'Sin coordenadas registradas'
                        : 'Coordenadas exactas ocultas (requiere ubicacion:ver_exacta)'}
                    </p>
                    {h.evidencias.length > 0 && (
                      <ul className="space-y-1">
                        {h.evidencias.map((ev) => (
                          <li key={ev.id} className="font-mono text-ink">
                            {ETIQUETA_EVIDENCIA[ev.tipo] ?? ev.tipo}
                            {ev.atributoTaxonomico ? ` · ${ev.atributoTaxonomico}` : ''}
                            {ev.valor ? `: ${ev.valor}${ev.unidad ? ` ${ev.unidad}` : ''}` : ''}
                            {ev.urlArchivo ? `: ${ev.urlArchivo}` : ''}
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                )}

                <div className="flex flex-wrap gap-2">
                  <Button
                    variant="ghost"
                    className="text-xs"
                    aria-expanded={expandido}
                    onClick={() => setAbierto(expandido ? null : h.id)}
                  >
                    {expandido ? 'Ocultar detalle' : 'Ver detalle'}
                  </Button>
                  {onModificar && !h.pendingSync && (
                    <Button variant="ghost" className="text-xs" onClick={() => onModificar(h.id)}>
                      Modificar o reclasificar
                    </Button>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}

      {filtrados.length > visibles && (
        <div className="flex justify-center">
          <Button variant="ghost" className="text-xs" onClick={() => setVisibles((n) => n + POR_PAGINA)}>
            Ver más hallazgos ({filtrados.length - visibles} restantes)
          </Button>
        </div>
      )}
    </Card>
  );
}
