import React from 'react';
import { Card } from '@/components/ui/Card';

export interface SeccionBloqueadaProps {
  titulo: string;
  descripcion: string;
}

/** Aviso con candado para una sección que el rol de la cuenta no tiene permitida. */
export function SeccionBloqueada({ titulo, descripcion }: SeccionBloqueadaProps) {
  return (
    <Card className="flex items-center gap-4">
      <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full border border-line bg-paper text-ink">
        <svg
          className="h-5 w-5"
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
        <p className="font-serif text-sm font-semibold text-ink">{titulo}</p>
        <p className="text-xs text-ink-muted">{descripcion}</p>
      </div>
    </Card>
  );
}
