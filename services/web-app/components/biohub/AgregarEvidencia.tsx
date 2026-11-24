'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { useNotificar } from '@/components/ui/Notificaciones';
import { agregarEvidencia, HallazgoResponse, TipoEvidencia } from '@/lib/registro-api';
import { avisoDeError } from '@/lib/avisos';
import { CLASE_INPUT, Encabezado } from './campo-ui';

export interface AgregarEvidenciaProps {
  hallazgos: HallazgoResponse[];
  onHallazgoActualizado: (hallazgo: HallazgoResponse) => void;
}

/** Suma una evidencia a un hallazgo ya registrado; el back la guarda como una nueva versión. */
export function AgregarEvidencia({ hallazgos, onHallazgoActualizado }: AgregarEvidenciaProps) {
  const [evHallazgoId, setEvHallazgoId] = useState('');
  const [evTipo, setEvTipo] = useState<TipoEvidencia>('TEMPERATURA');
  const [evValor, setEvValor] = useState('');
  const [evUnidad, setEvUnidad] = useState('C');
  const [evUrl, setEvUrl] = useState('');
  const [evAtributo, setEvAtributo] = useState('');
  const notificar = useNotificar();
  const hallazgoElegido = evHallazgoId || hallazgos[0]?.id || '';

  const handleAgregarEvidencia = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!hallazgoElegido) return;
    try {
      const actualizado = await agregarEvidencia(
        hallazgoElegido,
        {
          tipo: evTipo,
          valor: evTipo === 'AUDIO' ? undefined : evValor.trim(),
          unidad: evTipo === 'TEMPERATURA' ? evUnidad.trim() : undefined,
          urlArchivo: evTipo === 'AUDIO' ? evUrl.trim() : undefined,
          atributoTaxonomico: evAtributo.trim() || undefined,
        }
      );
      notificar({
        tipo: 'ok',
        titulo: 'Evidencia agregada',
        detalle: `El hallazgo ${actualizado.id.slice(0, 8)} quedó en la versión ${actualizado.versionActual}.`,
      });
      setEvValor('');
      setEvUrl('');
      setEvAtributo('');
      onHallazgoActualizado(actualizado);
    } catch (error) {
      notificar(
        avisoDeError(error, {
          titulo: 'No se agregó la evidencia',
          sinConexion: 'La evidencia no se guardó',
          accion: 'agregar evidencias',
        })
      );
    }
  };

  return (
    <Card className="space-y-4">
      <Encabezado
        titulo="Agregar evidencia a un hallazgo"
        descripcion="Suma una medición, muestra genética o grabación a un hallazgo ya registrado. Queda como una nueva versión en el historial."
      />
      <form onSubmit={handleAgregarEvidencia} className="grid grid-cols-1 md:grid-cols-12 gap-3 items-end">
        <Field id="ev-hallazgo" label="Hallazgo" className="md:col-span-5">
          <select
            id="ev-hallazgo"
            value={hallazgoElegido}
            onChange={(e) => setEvHallazgoId(e.target.value)}
            className={`${CLASE_INPUT} font-mono`}
          >
            {hallazgos.map((h) => (
              <option key={h.id} value={h.id}>
                [v{h.versionActual}] {h.especie} — {h.departamento} ({h.id.slice(0, 8)})
              </option>
            ))}
          </select>
        </Field>
        <Field id="ev-tipo" label="Tipo" className="md:col-span-3">
          <select
            id="ev-tipo"
            value={evTipo}
            onChange={(e) => setEvTipo(e.target.value as TipoEvidencia)}
            className={CLASE_INPUT}
          >
            <option value="TEMPERATURA">Temperatura</option>
            <option value="ADN">Muestra ADN</option>
            <option value="AUDIO">Audio bioacústico</option>
          </select>
        </Field>
        <Field id="ev-atributo" label="Parámetro medido (opcional)" className="md:col-span-4">
          <input
            id="ev-atributo"
            value={evAtributo}
            onChange={(e) => setEvAtributo(e.target.value)}
            placeholder="Temperatura del agua"
            className={CLASE_INPUT}
          />
        </Field>

        {evTipo === 'TEMPERATURA' && (
          <>
            <Field id="ev-valor" label="Valor numérico" className="md:col-span-4">
              <input
                id="ev-valor"
                required
                inputMode="decimal"
                value={evValor}
                onChange={(e) => setEvValor(e.target.value)}
                placeholder="28.5"
                className={`${CLASE_INPUT} font-mono`}
              />
            </Field>
            <Field id="ev-unidad" label="Unidad" className="md:col-span-2">
              <input
                id="ev-unidad"
                required
                value={evUnidad}
                onChange={(e) => setEvUnidad(e.target.value)}
                className={`${CLASE_INPUT} font-mono`}
              />
            </Field>
          </>
        )}
        {evTipo === 'ADN' && (
          <Field id="ev-codigo" label="Código de muestra genética" className="md:col-span-6">
            <input
              id="ev-codigo"
              required
              value={evValor}
              onChange={(e) => setEvValor(e.target.value)}
              placeholder="ADN-2026-901"
              className={`${CLASE_INPUT} font-mono`}
            />
          </Field>
        )}
        {evTipo === 'AUDIO' && (
          <Field id="ev-url" label="Enlace del archivo acústico" className="md:col-span-6">
            <input
              id="ev-url"
              type="url"
              required
              value={evUrl}
              onChange={(e) => setEvUrl(e.target.value)}
              placeholder="https://archivo.biohub.gov.co/canto.wav"
              className={`${CLASE_INPUT} font-mono`}
            />
          </Field>
        )}
        <div className="md:col-span-6">
          <Button type="submit" variant="ghost" className="w-full text-xs" disabled={!hallazgoElegido}>
            + Agregar evidencia
          </Button>
        </div>
      </form>
    </Card>
  );
}
