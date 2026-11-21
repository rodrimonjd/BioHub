import React from 'react';
import { Card } from '@/components/ui/Card';

export interface ApiUsagePanelProps {
  consumerName: string;
  endpointUrl: string;
  requestsPerMinute: number;
  limitPerMinute?: number;
  onSimulateRequest?: (delta: number) => void;
  onResetCounter?: () => void;
}

export function ApiUsagePanel({
  consumerName,
  endpointUrl,
  requestsPerMinute,
  limitPerMinute = 60,
  onSimulateRequest,
  onResetCounter,
}: ApiUsagePanelProps) {
  const ratio = requestsPerMinute / limitPerMinute;
  const percentage = Math.min(100, Math.round(ratio * 100));

  const isExceeded = requestsPerMinute > limitPerMinute;
  const isNearLimit = !isExceeded && ratio > 0.8;

  // Color moss-500 normal, sun-500 cerca del tope (>80%), iucn-cr al excederlo
  const barColorClass = isExceeded
    ? 'bg-iucn-cr'
    : isNearLimit
    ? 'bg-sun-500'
    : 'bg-moss-500';

  const statusLabel = isExceeded
    ? 'Límite excedido (HTTP 429 Too Many Requests)'
    : isNearLimit
    ? 'Cerca del límite por minuto (>80%)'
    : 'Tráfico dentro del umbral permitido';

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
        <span className="font-mono text-xs text-ink-muted">
          Máx. {limitPerMinute} req/min
        </span>
      </div>

      <div className="space-y-1 text-xs">
        <p className="text-ink font-medium">{consumerName}</p>
        <p className="font-mono text-ink-muted">Endpoint estable: {endpointUrl}</p>
      </div>

      <div className="space-y-2">
        <div className="flex items-baseline justify-between">
          <span className="text-xs font-medium text-ink">{statusLabel}</span>
          <span className="font-mono text-sm font-bold text-ink">
            {requestsPerMinute} / {limitPerMinute} req/min
          </span>
        </div>

        <div
          role="progressbar"
          aria-valuenow={requestsPerMinute}
          aria-valuemin={0}
          aria-valuemax={limitPerMinute}
          aria-label="Peticiones por minuto del consumidor externo"
          className="h-3 w-full overflow-hidden rounded-md border border-line bg-paper"
        >
          <div
            className={`h-full transition-all duration-150 ${barColorClass}`}
            style={{ width: `${percentage}%` }}
          />
        </div>
      </div>

      {onSimulateRequest && onResetCounter && (
        <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-line">
          <button
            type="button"
            onClick={() => onSimulateRequest(10)}
            className="rounded-md border border-line bg-paper px-3 py-1.5 font-mono text-xs text-ink hover:border-forest-700 transition-colors duration-150 cursor-pointer"
          >
            +10 peticiones
          </button>
          <button
            type="button"
            onClick={() => onSimulateRequest(25)}
            className="rounded-md border border-line bg-paper px-3 py-1.5 font-mono text-xs text-ink hover:border-forest-700 transition-colors duration-150 cursor-pointer"
          >
            Simular ráfaga (+25)
          </button>
          <button
            type="button"
            onClick={onResetCounter}
            className="rounded-md border border-line bg-surface px-3 py-1.5 font-mono text-xs text-ink-muted hover:text-ink transition-colors duration-150 cursor-pointer"
          >
            Reiniciar ventana
          </button>
        </div>
      )}
    </Card>
  );
}
