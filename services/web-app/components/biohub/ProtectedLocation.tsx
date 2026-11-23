import React from 'react';
import { Card } from '@/components/ui/Card';
import { ConservationBadge, ConservationStatus } from '@/components/ui/ConservationBadge';
export interface ProtectedLocationProps {
  /** Decisión tomada con los permisos de servicio-autorizacion (ubicacion:ver_exacta). */
  puedeVerExacta: boolean;
  /** Texto del nivel de visibilidad que se está mostrando (p. ej. el rol del usuario). */
  nivelLabel: string;
  speciesCommonName: string;
  speciesScientificName: string;
  status: ConservationStatus;
  regionLabel?: string;
  exactCoordinates: string;
  altitude: string;
}

export function ProtectedLocation({
  puedeVerExacta,
  nivelLabel,
  speciesCommonName,
  speciesScientificName,
  status,
  regionLabel = 'Región: Meta',
  exactCoordinates,
  altitude,
}: ProtectedLocationProps) {
  const isAuthorized = puedeVerExacta;

  return (
    <Card as="section" aria-labelledby="location-heading" className="space-y-5">
      <div className="flex flex-wrap items-start justify-between gap-2 border-b border-line pb-4">
        <div>
          <span className="font-mono text-xs uppercase tracking-wider text-ink-muted">
            Vigilancia del corredor biológico · Protección anticaza
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

      {/* Mapa real con contorno cartográfico de departamentos de la Orinoquía/Meta, ríos y polígono regional */}
      <div className="relative h-64 w-full overflow-hidden rounded-lg border border-line bg-paper">
        <svg
          className="h-full w-full text-ink"
          viewBox="0 0 420 240"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          role="img"
          aria-label={
            isAuthorized
              ? `Mapa cartográfico del corredor del Meta con punto exacto en ${exactCoordinates}`
              : `Mapa regional difuminado mostrando únicamente ${regionLabel}`
          }
        >
          {/* Retícula de coordenadas cartográficas */}
          <g stroke="currentColor" strokeOpacity="0.14" strokeWidth="0.6" strokeDasharray="3 3">
            <line x1="70" y1="0" x2="70" y2="240" />
            <line x1="140" y1="0" x2="140" y2="240" />
            <line x1="210" y1="0" x2="210" y2="240" />
            <line x1="280" y1="0" x2="280" y2="240" />
            <line x1="350" y1="0" x2="350" y2="240" />
            <line x1="0" y1="60" x2="420" y2="60" />
            <line x1="0" y1="120" x2="420" y2="120" />
            <line x1="0" y1="180" x2="420" y2="180" />
          </g>

          {/* Curvas de nivel de la Cordillera Oriental y Serranía de la Macarena */}
          <g stroke="currentColor" strokeOpacity="0.22" strokeWidth="1">
            <path d="M10 210 C55 150, 80 90, 125 20" />
            <path d="M32 225 C78 160, 105 95, 148 25" />
            <path d="M60 235 C105 170, 130 110, 175 30" />
          </g>

          {/* Cuencas hidrográficas: Río Guaviare, Río Ariari y Río Meta */}
          <g stroke="#2A7F8E" strokeOpacity="0.55" strokeWidth="1.6" strokeLinecap="round">
            <path d="M85 95 C145 102, 210 78, 310 58 C355 48, 385 40, 415 32" />
            <path d="M95 168 C165 162, 225 182, 315 165 C360 156, 390 148, 415 142" />
            <path d="M142 128 C185 135, 222 152, 255 172" strokeWidth="1.1" />
          </g>

          {/* Polígono cartográfico del Departamento del Meta / Corredor Macarena */}
          <path
            d="M115 72 L195 62 L278 78 L312 118 L288 172 L205 184 L132 166 L104 122 Z"
            fill="#6B9A5B"
            fillOpacity={isAuthorized ? '0.16' : '0.24'}
            stroke="currentColor"
            strokeOpacity="0.55"
            strokeWidth="1.3"
          />

          {/* Etiquetas cartográficas de referencia regional */}
          <text x="18" y="22" fill="currentColor" fillOpacity="0.55" fontSize="8" fontFamily="monospace">
            CUENCA ORINOQUÍA · COLOMBIA
          </text>
          <text x="315" y="52" fill="#2A7F8E" fontSize="8" fontFamily="monospace">
            Río Meta
          </text>
          <text x="295" y="182" fill="#2A7F8E" fontSize="8" fontFamily="monospace">
            Río Guaviare
          </text>

          {/* Si está autorizado muestra el marcador exacto; si es público muestra zona de amortiguación difuminada */}
          {isAuthorized ? (
            <g>
              <circle
                cx="188"
                cy="132"
                r="18"
                fill="#C23B2E"
                fillOpacity="0.18"
                stroke="#C23B2E"
                strokeWidth="1"
                strokeDasharray="2 2"
              />
              <circle cx="188" cy="132" r="4.5" fill="#C23B2E" />
              <circle cx="188" cy="132" r="2" fill="#FFFFFF" />
            </g>
          ) : (
            <g>
              <circle
                cx="205"
                cy="124"
                r="46"
                fill="#D9A441"
                fillOpacity="0.22"
                stroke="#D9A441"
                strokeWidth="1.2"
                strokeDasharray="4 3"
              />
            </g>
          )}
        </svg>

        {/* Tarjeta informativa superpuesta en la esquina inferior */}
        <div className="absolute inset-x-3 bottom-3 flex items-center justify-between rounded-lg border border-line bg-surface/95 px-4 py-3 backdrop-blur-[2px]">
          {!isAuthorized ? (
            <div className="flex items-center gap-3 text-left">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-line bg-paper text-ink">
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
              <div>
                <p className="font-serif text-sm font-semibold text-ink">
                  {regionLabel} (Polígono regional difuminado)
                </p>
                <p className="text-xs text-ink-muted">
                  Coordenadas exactas ocultas al público general para proteger a la especie de la caza ilegal.
                </p>
              </div>
            </div>
          ) : (
            <div className="space-y-0.5 text-left">
              <div className="inline-flex items-center gap-1.5 font-mono text-[11px] uppercase tracking-wider text-moss-500 font-semibold">
                <span className="h-2 w-2 rounded-full bg-moss-500" aria-hidden="true" />
                Coordenadas exactas verificadas
              </div>
              <p className="font-mono text-sm font-bold text-ink">{exactCoordinates}</p>
              <p className="font-mono text-xs text-ink-muted">
                {regionLabel} · Serranía de la Macarena · Altitud: {altitude}
              </p>
            </div>
          )}
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-ink-muted">
        <span>Nivel de visibilidad activo en esta vista:</span>
        <span className="font-mono font-medium text-ink">{nivelLabel}</span>
      </div>
    </Card>
  );
}
