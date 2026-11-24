'use client';

import React, { useState } from 'react';
import { Card } from '@/components/ui/Card';
import {
  ConservationBadge,
  ConservationStatus,
} from '@/components/ui/ConservationBadge';

export interface FlexibleAttribute {
  label: string;
  value: string;
}

export interface SpeciesCardProps {
  id: string;
  commonName: string;
  scientificName: string;
  status: ConservationStatus;
  specimenCode: string;
  attributes: FlexibleAttribute[];
  illustrationVariant?: 'amphibian' | 'avian' | 'reptile';
  /**
   * Desactiva la ficha de la especie con un motivo opcional. Solo se pasa si la especie tiene ficha
   * y la cuenta tiene el permiso ficha:desactivar; sin él la tarjeta no muestra el botón.
   */
  onDesactivar?: (motivo: string) => Promise<void>;
}

function BotanicalIllustration({
  variant = 'amphibian',
}: {
  variant?: 'amphibian' | 'avian' | 'reptile';
}) {
  return (
    <div
      className="relative flex h-40 w-full items-center justify-center overflow-hidden rounded-lg border border-line bg-paper px-6 py-4"
      aria-hidden="true"
    >
      <svg
        className="h-28 w-full text-ink opacity-75"
        viewBox="0 0 240 110"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <rect
          x="4"
          y="4"
          width="232"
          height="102"
          stroke="currentColor"
          strokeWidth="0.6"
          strokeDasharray="2 2"
          opacity="0.35"
        />
        {variant === 'amphibian' && (
          <g stroke="currentColor" strokeWidth="1.1" strokeLinecap="round">
            <path d="M35 85 C75 65, 135 45, 200 22" opacity="0.45" />
            <path d="M75 68 C85 45, 118 38, 145 48 C162 54, 170 70, 148 78 C122 86, 86 80, 75 68 Z" />
            <path d="M95 54 C112 52, 130 56, 145 64" strokeWidth="0.75" opacity="0.65" />
            <circle cx="152" cy="54" r="2.2" fill="currentColor" />
            <path d="M88 76 L68 92 M72 90 L62 93 M72 90 L70 96" />
            <path d="M136 77 L154 91 M154 91 L163 90 M154 91 L157 96" />
            <path d="M50 32 C68 24, 92 26, 106 38" strokeWidth="0.6" strokeDasharray="3 3" />
          </g>
        )}
        {variant === 'avian' && (
          <g stroke="currentColor" strokeWidth="1.1" strokeLinecap="round">
            <path d="M38 78 C78 38, 138 26, 198 34" />
            <path d="M56 64 C94 42, 144 36, 186 44" strokeWidth="0.8" />
            <path
              d="M74 53 L64 38 M94 45 L86 28 M116 41 L112 24 M138 39 L136 24 M160 40 L162 27"
              strokeWidth="0.75"
              opacity="0.7"
            />
            <path d="M38 78 C68 74, 124 66, 178 48" strokeWidth="0.85" />
            <circle cx="48" cy="72" r="2" fill="currentColor" />
            <circle cx="192" cy="35" r="2" fill="currentColor" />
          </g>
        )}
        {variant === 'reptile' && (
          <g stroke="currentColor" strokeWidth="1.1" strokeLinecap="round">
            <path d="M24 58 C64 44, 118 44, 168 54 L212 50" />
            <path d="M24 58 C64 72, 118 72, 168 58 L212 50" />
            <path
              d="M60 48 L64 42 L68 48 M78 47 L82 40 L86 47 M96 47 L100 40 L104 47 M114 48 L118 41 L122 48"
              strokeWidth="0.8"
            />
            <circle cx="182" cy="51" r="1.8" fill="currentColor" />
          </g>
        )}
        <text
          x="14"
          y="98"
          fill="currentColor"
          fontSize="7"
          fontFamily="monospace"
          opacity="0.6"
        >
          LÁMINA TAXONÓMICA · ARCHIVO CIENTÍFICO BIOHUB
        </text>
      </svg>
    </div>
  );
}

export function SpeciesCard({
  id,
  commonName,
  scientificName,
  status,
  specimenCode,
  attributes,
  illustrationVariant = 'amphibian',
  onDesactivar,
}: SpeciesCardProps) {
  const [expanded, setExpanded] = useState<boolean>(false);
  const detailsId = `species-attributes-${id}`;
  // Confirmación antes de desactivar la ficha
  const [confirmando, setConfirmando] = useState<boolean>(false);
  const [motivo, setMotivo] = useState<string>('');
  const [desactivando, setDesactivando] = useState<boolean>(false);
  const [errorDesactivar, setErrorDesactivar] = useState<string | null>(null);

  const handleDesactivar = async () => {
    if (!onDesactivar) return;
    setDesactivando(true);
    setErrorDesactivar(null);
    try {
      await onDesactivar(motivo);
      setConfirmando(false);
      setMotivo('');
    } catch (error) {
      setErrorDesactivar(error instanceof Error ? error.message : 'No se pudo desactivar la ficha.');
    } finally {
      setDesactivando(false);
    }
  };

  return (
    <Card as="article" className="flex flex-col justify-between space-y-4">
      <div className="space-y-3.5">
        <BotanicalIllustration variant={illustrationVariant} />

        <div className="flex flex-wrap items-start justify-between gap-2">
          <div>
            <p className="font-mono text-xs text-ink-muted">
              FICHA #{id} · {specimenCode}
            </p>
            <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
              {commonName}
            </h3>
            <p className="font-serif italic text-base text-ink-muted">
              {scientificName}
            </p>
          </div>
          <ConservationBadge status={status} />
        </div>
      </div>

      {/* Botón desplegable accesible en móvil y escritorio para ver atributos */}
      <div className="border-t border-line pt-3">
        <button
          type="button"
          onClick={() => setExpanded((prev) => !prev)}
          aria-expanded={expanded}
          aria-controls={detailsId}
          className="flex min-h-11 w-full items-center justify-between rounded-md border border-line bg-paper px-3.5 py-2 text-xs font-medium text-ink hover:border-forest-700 transition-colors duration-150 cursor-pointer"
        >
          <span>
            {expanded
              ? `Ocultar atributos (${attributes.length})`
              : `Ver atributos (${attributes.length})`}
          </span>
          <span className="font-mono text-xs text-ink-muted" aria-hidden="true">
            {expanded ? '▲' : '▼'}
          </span>
        </button>

        {expanded && (
          <dl
            id={detailsId}
            className="mt-3 grid grid-cols-1 gap-2"
          >
            {attributes.map((attr) => (
              <div
                key={`${id}-${attr.label}`}
                className="rounded-md border border-line bg-paper px-3 py-2"
              >
                <dt className="text-xs text-ink-muted">{attr.label}</dt>
                <dd className="mt-0.5 font-mono text-xs font-medium text-ink">
                  {attr.value}
                </dd>
              </div>
            ))}
          </dl>
        )}

        {onDesactivar && (
          <div className="mt-3 space-y-2">
            {!confirmando ? (
              <button
                type="button"
                onClick={() => {
                  setConfirmando(true);
                  setErrorDesactivar(null);
                }}
                className="flex min-h-11 w-full cursor-pointer items-center justify-center rounded-md border border-iucn-cr/50 px-3.5 py-2 text-xs font-medium text-ink transition-colors duration-150 hover:bg-iucn-cr/10"
              >
                Desactivar ficha
              </button>
            ) : (
              <div className="space-y-2 rounded-md border border-iucn-cr/50 bg-iucn-cr/5 p-3">
                <p className="text-xs text-ink">
                  La ficha de <span className="font-semibold">{commonName}</span> quedará inactiva. No se borra: el
                  cambio queda en el historial.
                </p>
                <label className="block space-y-1 text-xs text-ink">
                  <span className="font-medium">Motivo (opcional)</span>
                  <input
                    type="text"
                    value={motivo}
                    maxLength={500}
                    onChange={(e) => setMotivo(e.target.value)}
                    placeholder="Ej. ficha duplicada o especie reclasificada"
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  />
                </label>
                {errorDesactivar && (
                  <p role="alert" className="rounded-md border border-iucn-cr bg-iucn-cr/10 px-3 py-2 font-mono text-xs text-ink">
                    {errorDesactivar}
                  </p>
                )}
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => setConfirmando(false)}
                    disabled={desactivando}
                    className="min-h-11 cursor-pointer rounded-md border border-line bg-paper px-3 text-xs text-ink hover:border-forest-700"
                  >
                    Cancelar
                  </button>
                  <button
                    type="button"
                    onClick={handleDesactivar}
                    disabled={desactivando}
                    className="min-h-11 cursor-pointer rounded-md bg-iucn-cr px-3 text-xs font-semibold text-white hover:opacity-90 disabled:opacity-60"
                  >
                    {desactivando ? 'Desactivando...' : 'Confirmar desactivación'}
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </Card>
  );
}
