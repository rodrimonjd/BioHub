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
}: SpeciesCardProps) {
  const [expanded, setExpanded] = useState<boolean>(false);
  const detailsId = `species-attributes-${id}`;

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
      </div>
    </Card>
  );
}
