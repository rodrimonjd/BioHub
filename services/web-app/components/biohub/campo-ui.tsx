'use client';

import React from 'react';

/** Piezas de interfaz compartidas por los formularios de campo (equipos, expediciones y evidencias). */
export const CLASE_INPUT =
  'min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink focus:outline-none focus:ring-2 focus:ring-moss-500';

export function Encabezado({ titulo, descripcion }: { titulo: string; descripcion: string }) {
  return (
    <div className="border-b border-line pb-3">
      <h3 className="font-serif text-lg font-semibold text-ink">{titulo}</h3>
      <p className="text-xs text-ink-muted">{descripcion}</p>
    </div>
  );
}
