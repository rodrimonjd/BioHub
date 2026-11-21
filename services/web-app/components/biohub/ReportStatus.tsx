import React from 'react';
import { Card } from '@/components/ui/Card';

export type BackgroundReportState = 'en_cola' | 'generando' | 'listo';

export interface ReportStatusProps {
  reportTitle: string;
  reportId: string;
  recordsProcessed: string;
  state: BackgroundReportState;
  onChangeState?: (nextState: BackgroundReportState) => void;
}

const STATE_DETAILS: Record<
  BackgroundReportState,
  { badgeText: string; description: string; dotColor: string }
> = {
  en_cola: {
    badgeText: 'En cola',
    description:
      'El reporte mensual fue agendado en el bróker de eventos. El registro diario de avistamientos opera sin bloqueos.',
    dotColor: 'bg-sun-500',
  },
  generando: {
    badgeText: 'Generando en segundo plano',
    description:
      'Cruzando millones de registros históricos para calcular índices de presión sobre ecosistemas y zonas de deforestación.',
    dotColor: 'bg-water-600',
  },
  listo: {
    badgeText: 'Listo para descarga',
    description:
      'Cálculo analítico finalizado de forma independiente sin afectar la disponibilidad de las estaciones de campo.',
    dotColor: 'bg-iucn-lc',
  },
};

export function ReportStatus({
  reportTitle,
  reportId,
  recordsProcessed,
  state,
  onChangeState,
}: ReportStatusProps) {
  const info = STATE_DETAILS[state];

  return (
    <Card as="section" aria-labelledby="report-status-heading" className="space-y-4">
      <div className="flex flex-wrap items-start justify-between gap-2 border-b border-line pb-3">
        <div>
          <span className="font-mono text-xs uppercase tracking-wider text-water-600 font-semibold">
            Analítica desacoplada · Segundo plano
          </span>
          <h3 id="report-status-heading" className="mt-1 font-serif text-lg font-semibold text-ink">
            {reportTitle}
          </h3>
        </div>
        <span className="font-mono text-xs text-ink-muted">ID: {reportId}</span>
      </div>

      <div
        role="status"
        aria-live="polite"
        className="flex items-center justify-between rounded-lg border border-line bg-paper px-4 py-3"
      >
        <div className="flex items-center gap-3">
          {state === 'generando' ? (
            <svg
              className="h-4 w-4 animate-spin text-water-600 shrink-0"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="3"
              />
              <path
                className="opacity-90"
                fill="currentColor"
                d="M4 12a8 8 0 018-8v3a5 5 0 00-5 5H4z"
              />
            </svg>
          ) : (
            <span className={`h-2.5 w-2.5 rounded-full shrink-0 ${info.dotColor}`} aria-hidden="true" />
          )}
          <span className="text-sm font-medium text-ink">{info.badgeText}</span>
        </div>

        <span className="font-mono text-xs text-ink-muted">
          Registros: {recordsProcessed}
        </span>
      </div>

      <p className="text-xs text-ink-muted leading-relaxed">{info.description}</p>

      {onChangeState && (
        <div
          className="flex flex-wrap items-center gap-2 pt-2 border-t border-line"
          role="group"
          aria-label="Cambiar estado del reporte en segundo plano"
        >
          {(['en_cola', 'generando', 'listo'] as BackgroundReportState[]).map((itemState) => {
            const active = state === itemState;
            return (
              <button
                key={itemState}
                type="button"
                aria-pressed={active}
                onClick={() => onChangeState(itemState)}
                className={`rounded-md px-3 py-1.5 font-mono text-xs transition-colors duration-150 cursor-pointer border ${
                  active
                    ? 'bg-forest-900 text-white border-forest-900'
                    : 'bg-paper text-ink border-line hover:border-forest-700'
                }`}
              >
                {itemState === 'en_cola' && '1. En cola'}
                {itemState === 'generando' && '2. Generando'}
                {itemState === 'listo' && '3. Listo'}
              </button>
            );
          })}
        </div>
      )}
    </Card>
  );
}
