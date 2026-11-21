import React from 'react';
import { Card } from '@/components/ui/Card';

export interface HistoryEventItem {
  id: string;
  version: string;
  date: string;
  previousBelief: string;
  currentKnowledge: string;
  authorizedBy: string;
  reason: string;
}

export interface HistoryTimelineProps {
  findingId: string;
  events: HistoryEventItem[];
}

export function HistoryTimeline({ findingId, events }: HistoryTimelineProps) {
  return (
    <Card as="section" aria-labelledby="timeline-heading" className="space-y-6">
      <div className="flex flex-wrap items-baseline justify-between gap-2 border-b border-line pb-4">
        <div>
          <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
            Trazabilidad inmutable · Nunca se borra un evento anterior
          </span>
          <h3 id="timeline-heading" className="mt-1 font-serif text-xl font-semibold text-ink">
            Historial de reclasificación científica
          </h3>
        </div>
        <span className="font-mono text-xs text-ink-muted">
          Hallazgo: {findingId}
        </span>
      </div>

      <ol className="relative border-l border-line ml-3 space-y-6">
        {events.map((event) => (
          <li key={event.id} className="ml-6">
            {/* Nodo indicador en la línea vertical */}
            <span
              className="absolute -left-2.5 mt-1.5 flex h-5 w-5 items-center justify-center rounded-full border border-forest-700 bg-paper font-mono text-[10px] font-bold text-ink"
              aria-hidden="true"
            >
              {event.version}
            </span>

            <div className="rounded-lg border border-line bg-paper p-4 space-y-3">
              <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line pb-2">
                <span className="font-mono text-xs font-semibold text-ink">
                  Evento {event.id}
                </span>
                <time className="font-mono text-xs text-ink-muted" dateTime={event.date}>
                  {event.date}
                </time>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div className="rounded-md border border-line bg-surface p-3">
                  <p className="font-mono text-[11px] uppercase tracking-wider text-ink-muted">
                    Qué se creía
                  </p>
                  <p className="mt-1 text-sm text-ink">
                    <span className="font-serif italic">{event.previousBelief}</span>
                  </p>
                </div>

                <div className="rounded-md border border-moss-500/40 bg-surface p-3">
                  <p className="font-mono text-[11px] uppercase tracking-wider text-moss-500 font-semibold">
                    Qué se sabe ahora
                  </p>
                  <p className="mt-1 text-sm font-medium text-ink">
                    <span className="font-serif italic">{event.currentKnowledge}</span>
                  </p>
                </div>
              </div>

              <div className="pt-1 text-xs text-ink-muted space-y-1">
                <p>
                  <strong className="text-ink font-medium">Autorizado por:</strong>{' '}
                  <span className="font-mono text-ink">{event.authorizedBy}</span>
                </p>
                <p>
                  <strong className="text-ink font-medium">Justificación científica:</strong>{' '}
                  {event.reason}
                </p>
              </div>
            </div>
          </li>
        ))}
      </ol>
    </Card>
  );
}
