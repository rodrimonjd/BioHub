'use client';

import React from 'react';
import { ErrorRechazado } from '@/lib/registro-api';

/** Piezas de interfaz compartidas por los formularios de campo (equipos, expediciones y evidencias). */
export type Aviso = { tipo: 'ok' | 'err'; texto: string } | null;

export const CLASE_INPUT =
  'min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink focus:outline-none focus:ring-2 focus:ring-moss-500';

export function mensajeDeError(error: unknown): string {
  return error instanceof ErrorRechazado
    ? error.message
    : 'No se pudo conectar con el servicio de registro.';
}

export function MensajeAviso({ aviso }: { aviso: Aviso }) {
  return (
    <div aria-live="polite">
      {aviso && (
        <p
          role={aviso.tipo === 'err' ? 'alert' : 'status'}
          className={`rounded-md border px-3 py-2 font-mono text-xs text-ink ${
            aviso.tipo === 'ok' ? 'border-iucn-lc bg-iucn-lc/10' : 'border-iucn-cr bg-iucn-cr/10'
          }`}
        >
          {aviso.texto}
        </p>
      )}
    </div>
  );
}

export function Encabezado({ titulo, descripcion }: { titulo: string; descripcion: string }) {
  return (
    <div className="border-b border-line pb-3">
      <h3 className="font-serif text-lg font-semibold text-ink">{titulo}</h3>
      <p className="text-xs text-ink-muted">{descripcion}</p>
    </div>
  );
}
