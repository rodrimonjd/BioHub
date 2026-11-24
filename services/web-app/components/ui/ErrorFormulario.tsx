import React from 'react';

/** Error de un dato del formulario: va junto al botón de enviar para que se sepa qué corregir. */
export function ErrorFormulario({ texto }: { texto: string | null | undefined }) {
  if (!texto) return null;
  return (
    <p
      role="alert"
      className="flex items-start gap-2.5 rounded-md border border-iucn-cr/50 bg-iucn-cr/10 px-3 py-2.5 text-[13px] leading-snug text-ink"
    >
      <svg
        viewBox="0 0 16 16"
        className="mt-0.5 h-4 w-4 shrink-0 text-iucn-cr"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        aria-hidden="true"
      >
        <circle cx="8" cy="8" r="6.4" />
        <path d="M8 4.8 L8 8.6 M8 11.1 L8 11.2" />
      </svg>
      {texto}
    </p>
  );
}
