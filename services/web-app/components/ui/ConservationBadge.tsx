import React from 'react';

export type ConservationStatus = 'LC' | 'NT' | 'VU' | 'EN' | 'CR';

export interface ConservationBadgeProps {
  status: ConservationStatus;
  className?: string;
}

const STATUS_CONFIG: Record<
  ConservationStatus,
  { label: string; bgClass: string; textClass: string }
> = {
  LC: {
    label: 'Preocupación menor',
    bgClass: 'bg-iucn-lc',
    textClass: 'text-white',
  },
  NT: {
    label: 'Casi amenazada',
    bgClass: 'bg-iucn-nt',
    textClass: 'text-[#1B2A22]',
  },
  VU: {
    label: 'Vulnerable',
    bgClass: 'bg-iucn-vu',
    textClass: 'text-[#1B2A22]', // Regla de accesibilidad: amarillo iucn-vu SIEMPRE con texto ink (#1B2A22), nunca blanco
  },
  EN: {
    label: 'En peligro',
    bgClass: 'bg-iucn-en',
    textClass: 'text-white',
  },
  CR: {
    label: 'En peligro crítico',
    bgClass: 'bg-iucn-cr',
    textClass: 'text-white',
  },
};

export function ConservationBadge({ status, className = '' }: ConservationBadgeProps) {
  const config = STATUS_CONFIG[status];

  return (
    <span
      role="status"
      aria-label={`Estado de conservación UICN: ${status} — ${config.label}`}
      className={`inline-flex items-center gap-1.5 rounded-md px-2.5 py-1 text-xs font-medium tracking-wide ${config.bgClass} ${config.textClass} ${className}`}
    >
      <span className="font-mono font-bold">{status}</span>
      <span aria-hidden="true" className="opacity-75">
        ·
      </span>
      <span>{config.label}</span>
    </span>
  );
}
