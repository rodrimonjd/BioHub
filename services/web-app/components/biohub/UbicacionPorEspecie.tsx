'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { Field } from '@/components/ui/Field';
import { ConservationStatus } from '@/components/ui/ConservationBadge';
import { ProtectedLocation } from './ProtectedLocation';
import { listarHallazgosPublicos, RECONSULTAS_MS } from '@/lib/consulta-api';
import { BioHubUser, canViewExactCoordinates } from '@/lib/permissions';
import { HallazgoResponse } from '@/lib/registro-api';
import { useAccesoUbicacion } from '@/lib/useAccesoUbicacion';
import { EspecieRegistro } from '@/lib/taxonomia-api';

export interface UbicacionPorEspecieProps {
  usuario: BioHubUser;
  especies: EspecieRegistro[];
  /** Cambia cada vez que se registra o modifica un hallazgo, para volver a consultarlos. */
  recarga?: number;
}

const ESTADOS: ConservationStatus[] = ['LC', 'NT', 'VU', 'EN', 'CR'];

/** Convierte grados decimales a grados, minutos y segundos (02° 42' 45.0" N). */
function aGrados(valor: number, positivo: string, negativo: string): string {
  const absoluto = Math.abs(valor);
  const grados = Math.floor(absoluto);
  const minutosDecimales = (absoluto - grados) * 60;
  const minutos = Math.floor(minutosDecimales);
  const segundos = (minutosDecimales - minutos) * 60;
  const dos = (n: number) => String(n).padStart(2, '0');
  return `${dos(grados)}° ${dos(minutos)}' ${segundos.toFixed(1).padStart(4, '0')}" ${
    valor >= 0 ? positivo : negativo
  }`;
}

/**
 * Ubicación del hallazgo más reciente de la especie elegida (HU-06 y HU-07), leída de servicio-consulta-publica:
 * exacta con ubicacion:ver_exacta o con acceso a esa especie; si no, solo la región o el departamento.
 */
export function UbicacionPorEspecie({ usuario, especies, recarga = 0 }: UbicacionPorEspecieProps) {
  const [hallazgos, setHallazgos] = useState<HallazgoResponse[]>([]);
  const [especieId, setEspecieId] = useState('');
  const puedeVer = useAccesoUbicacion(usuario);

  useEffect(() => {
    let vigente = true;
    const consultar = () =>
      listarHallazgosPublicos(usuario.id).then(({ data }) => {
        if (vigente && data) setHallazgos(data);
      });
    consultar();
    const esperas = recarga > 0 ? RECONSULTAS_MS.map((ms) => setTimeout(consultar, ms)) : [];
    return () => {
      vigente = false;
      esperas.forEach(clearTimeout);
    };
  }, [usuario.id, recarga]);

  // Hallazgo más reciente de cada especie
  const ultimoPorEspecie = useMemo(() => {
    const mapa = new Map<string, HallazgoResponse>();
    for (const h of hallazgos) {
      const actual = mapa.get(h.especieId);
      if (!actual || h.fecha > actual.fecha) mapa.set(h.especieId, h);
    }
    return mapa;
  }, [hallazgos]);

  // Por defecto, la primera especie que ya tenga hallazgos registrados
  const elegida =
    especies.find((e) => e.id === especieId) ??
    especies.find((e) => ultimoPorEspecie.has(e.id)) ??
    especies[0];

  // Sin inventario (taxonomía caída o vacía) se muestra igual el aviso de visibilidad
  if (!elegida) {
    return (
      <ProtectedLocation
        puedeVerExacta={canViewExactCoordinates(usuario)}
        nivelLabel={usuario.roleLabel}
        speciesCommonName="Sin especies en el inventario"
        speciesScientificName="—"
        status="LC"
        exactCoordinates=""
        sinHallazgos
      />
    );
  }

  const hallazgo = ultimoPorEspecie.get(elegida.id);
  const estado = ESTADOS.includes(elegida.estadoConservacion as ConservationStatus)
    ? (elegida.estadoConservacion as ConservationStatus)
    : 'LC';
  const coordenadas =
    hallazgo?.latitud != null && hallazgo?.longitud != null
      ? `${aGrados(hallazgo.latitud, 'N', 'S')} · ${aGrados(hallazgo.longitud, 'E', 'W')}`
      : null;
  const region = hallazgo
    ? hallazgo.region
      ? `Región: ${hallazgo.departamento} · ${hallazgo.region}`
      : `Departamento: ${hallazgo.departamento}`
    : undefined;

  return (
    <ProtectedLocation
      puedeVerExacta={puedeVer(elegida.id)}
      nivelLabel={usuario.roleLabel}
      speciesCommonName={elegida.nombreComun ?? elegida.nombreCientifico}
      speciesScientificName={elegida.nombreCientifico}
      status={estado}
      regionLabel={region}
      exactCoordinates={coordenadas ?? ''}
      sinHallazgos={!hallazgo || (puedeVer(elegida.id) && coordenadas === null)}
      selector={
        <Field id="ubicacion-especie" label="Especie a consultar">
          <select
            id="ubicacion-especie"
            value={elegida.id}
            onChange={(e) => setEspecieId(e.target.value)}
            className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
          >
            {especies.map((e) => (
              <option key={e.id} value={e.id}>
                {e.nombreComun ?? e.nombreCientifico} ({e.nombreCientifico})
                {ultimoPorEspecie.has(e.id) ? '' : ' · sin hallazgos'}
              </option>
            ))}
          </select>
        </Field>
      }
    />
  );
}
