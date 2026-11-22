import React from 'react';
import { Card } from '@/components/ui/Card';
import { ConservationBadge, ConservationStatus } from '@/components/ui/ConservationBadge';
import { canViewExactCoordinates, UserRole } from '@/lib/permissions';

export interface ProtectedLocationProps {
  role: UserRole;
  speciesCommonName: string;
  speciesScientificName: string;
  status: ConservationStatus;
  regionLabel?: string;
  exactCoordinates: string;
  altitude: string;
}

const ROLE_LABELS: Record<UserRole, string> = {
  public: 'Visitante común',
  researcher: 'Investigador de campo (sin acceso a coordenadas CR/EN)',
  senior: 'Personal científico de alto rango',
  auditor: 'Auditor científico',
  director: 'Director científico',
};

export function ProtectedLocation({
  role,
  speciesCommonName,
  speciesScientificName,
  status,
  regionLabel = 'Región: Meta',
  exactCoordinates,
  altitude,
}: ProtectedLocationProps) {
  const isAuthorized = canViewExactCoordinates(role);

  return (
    <Card as="section" aria-labelledby="location-heading" className="space-y-5">
      <div className="flex flex-wrap items-start justify-between gap-2 border-b border-line pb-4">
        <div>
          <span className="font-mono text-xs uppercase tracking-wider text-ink-muted">
            Vigilancia del corredor biológico · Protección anticaza (HU-06 / HU-07)
          </span>
          <h3 id="location-heading" className="mt-1 font-serif text-xl font-semibold text-ink">
            Ubicación geográfica de especie sensible
          </h3>
          <p className="text-sm text-ink-muted">
            {speciesCommonName} (<span className="font-serif italic">{speciesScientificName}</span>)
          </p>
        </div>
        <ConservationBadge status={status} />
      </div>

      {/* Lienzo cartográfico con curvas de nivel */}
      <div className="relative h-56 w-full overflow-hidden rounded-lg border border-line bg-paper">
        <svg
          className={`h-full w-full text-ink transition-all duration-200 ${
            isAuthorized ? 'opacity-30' : 'opacity-20 blur-[3px]'
          }`}
          viewBox="0 0 400 220"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
        >
          <path
            d="M-20 40 C60 20, 140 80, 220 50 C300 20, 360 70, 420 40"
            stroke="currentColor"
            strokeWidth="1"
          />
          <path
            d="M-20 85 C70 65, 130 120, 230 90 C320 60, 350 110, 420 85"
            stroke="currentColor"
            strokeWidth="1"
          />
          <path
            d="M-20 130 C80 110, 150 165, 240 135 C330 105, 370 150, 420 130"
            stroke="currentColor"
            strokeWidth="1"
          />
          <path
            d="M-20 175 C90 155, 160 200, 250 175 C340 150, 380 190, 420 175"
            stroke="currentColor"
            strokeWidth="1"
          />
          <line
            x1="100"
            y1="0"
            x2="100"
            y2="220"
            stroke="currentColor"
            strokeWidth="0.5"
            strokeDasharray="3 3"
          />
          <line
            x1="200"
            y1="0"
            x2="200"
            y2="220"
            stroke="currentColor"
            strokeWidth="0.5"
            strokeDasharray="3 3"
          />
          <line
            x1="300"
            y1="0"
            x2="300"
            y2="220"
            stroke="currentColor"
            strokeWidth="0.5"
            strokeDasharray="3 3"
          />
          <line
            x1="0"
            y1="110"
            x2="400"
            y2="110"
            stroke="currentColor"
            strokeWidth="0.5"
            strokeDasharray="3 3"
          />
        </svg>

        {/* Capa superpuesta según rol validado por lib/permissions.ts */}
        <div className="absolute inset-0 flex flex-col items-center justify-center p-6 text-center">
          {!isAuthorized ? (
            <div className="rounded-lg border border-line bg-surface/95 px-5 py-4 max-w-sm space-y-2">
              <div className="mx-auto flex h-9 w-9 items-center justify-center rounded-full border border-line bg-paper text-ink">
                <svg
                  className="h-4 w-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </div>
              <p className="font-serif text-base font-semibold text-ink">{regionLabel}</p>
              <p className="text-xs text-ink-muted">
                Coordenadas exactas restringidas al público general para proteger a la especie de la caza ilegal.
              </p>
            </div>
          ) : (
            <div className="rounded-lg border border-moss-500 bg-surface/95 px-5 py-4 max-w-sm space-y-1.5">
              <div className="inline-flex items-center gap-1.5 font-mono text-[11px] uppercase tracking-wider text-moss-500 font-semibold">
                <span className="h-2 w-2 rounded-full bg-moss-500" aria-hidden="true" />
                Acceso científico autorizado (HU-07)
              </div>
              <p className="font-mono text-sm font-bold text-ink">{exactCoordinates}</p>
              <p className="font-mono text-xs text-ink-muted">
                {regionLabel} · Altitud: {altitude}
              </p>
            </div>
          )}
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-ink-muted">
        <span>
          Autoridad de validación: <code className="font-mono text-ink">lib/permissions.ts</code>
        </span>
        <span className="font-mono">Rol activo: {ROLE_LABELS[role]}</span>
      </div>
    </Card>
  );
}
