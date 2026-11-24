'use client';

import { useCallback, useEffect, useState } from 'react';
import { AccesoUbicacion, obtenerAccesoUbicacion } from '@/lib/autorizacion-api';
import { BioHubUser, canViewExactCoordinates } from '@/lib/permissions';

/**
 * Qué especies ve la cuenta con coordenadas exactas: todas si tiene ubicacion:ver_exacta,
 * o solo las que la autoridad ambiental le dio. El back aplica la misma regla y no envía las demás coordenadas;
 * esto solo decide qué mensaje mostrar ("ocultas" o "sin registrar").
 */
export function useAccesoUbicacion(usuario: BioHubUser | null | undefined): (especieId: string) => boolean {
  const [acceso, setAcceso] = useState<AccesoUbicacion | null>(null);
  const usuarioId = usuario?.id;

  useEffect(() => {
    if (!usuarioId) return;
    let vigente = true;
    obtenerAccesoUbicacion(usuarioId).then((a) => {
      if (vigente) setAcceso(a);
    });
    return () => {
      vigente = false;
    };
  }, [usuarioId]);

  const todas = canViewExactCoordinates(usuario) || acceso?.todas === true;
  const especies = acceso?.especies;
  return useCallback(
    (especieId: string) => todas || (especies ?? []).includes(especieId),
    [todas, especies]
  );
}
