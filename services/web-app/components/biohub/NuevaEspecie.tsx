'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { ConservationStatus } from '@/components/ui/ConservationBadge';
import { useNotificar } from '@/components/ui/Notificaciones';
import { avisoDeError } from '@/lib/avisos';
import { crearEspecie } from '@/lib/registro-api';

export interface NuevaEspecieProps {
  /** Se llama tras guardar, para recargar el inventario desde la base de datos. */
  onCreada: () => Promise<void> | void;
}

const CLASE_INPUT =
  'min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink focus:outline-none focus:ring-2 focus:ring-moss-500';

const CATEGORIAS: { valor: ConservationStatus; etiqueta: string }[] = [
  { valor: 'LC', etiqueta: 'LC · Preocupación menor' },
  { valor: 'NT', etiqueta: 'NT · Casi amenazada' },
  { valor: 'VU', etiqueta: 'VU · Vulnerable' },
  { valor: 'EN', etiqueta: 'EN · En peligro' },
  { valor: 'CR', etiqueta: 'CR · En peligro crítico' },
];

function nombreCategoria(estado: ConservationStatus): string {
  return CATEGORIAS.find((c) => c.valor === estado)?.etiqueta.split(' · ')[1] ?? estado;
}

/** Formulario para agregar una especie al catálogo (servicio-registro, permiso hallazgo:crear). */
export function NuevaEspecie({ onCreada }: NuevaEspecieProps) {
  const [cientifico, setCientifico] = useState<string>('');
  const [comun, setComun] = useState<string>('');
  const [estado, setEstado] = useState<ConservationStatus>('LC');
  const [guardando, setGuardando] = useState<boolean>(false);
  const notificar = useNotificar();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cientifico.trim()) return;
    setGuardando(true);
    try {
      const creada = await crearEspecie(
        {
          nombreCientifico: cientifico.trim(),
          nombreComun: comun.trim() || null,
          estadoConservacion: estado,
        }
      );
      notificar({
        tipo: 'ok',
        titulo: 'Especie agregada',
        detalle: `${creada.nombreComun ?? creada.nombreCientifico} ya está en el catálogo como ${nombreCategoria(
          creada.estadoConservacion
        )}.`,
      });
      setCientifico('');
      setComun('');
      setEstado('LC');
      await onCreada();
    } catch (error) {
      notificar(
        avisoDeError(error, {
          titulo: 'No se agregó la especie',
          sinConexion: 'La especie no se guardó',
          accion: 'agregar especies al catálogo',
        })
      );
    } finally {
      setGuardando(false);
    }
  };

  return (
    <Card className="bg-surface space-y-4">
      <div className="border-b border-line pb-3">
        <h3 className="font-serif text-lg font-semibold text-ink">Agregar especie al catálogo</h3>
        <p className="text-xs text-ink-muted">
          Registra una especie que aún no está en el inventario. Luego podrás crear su ficha
          taxonómica agregándole atributos.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-12 gap-3 items-end">
        <Field id="esp-cientifico" label="Nombre científico" className="md:col-span-4">
          <input
            id="esp-cientifico"
            type="text"
            required
            maxLength={150}
            value={cientifico}
            onChange={(e) => setCientifico(e.target.value)}
            placeholder="Ej. Tremarctos ornatus"
            className={`${CLASE_INPUT} italic`}
          />
        </Field>

        <Field id="esp-comun" label="Nombre común" className="md:col-span-3">
          <input
            id="esp-comun"
            type="text"
            maxLength={150}
            value={comun}
            onChange={(e) => setComun(e.target.value)}
            placeholder="Ej. Oso de anteojos"
            className={CLASE_INPUT}
          />
        </Field>

        <Field id="esp-estado" label="Categoría UICN" className="md:col-span-3">
          <select
            id="esp-estado"
            value={estado}
            onChange={(e) => setEstado(e.target.value as ConservationStatus)}
            className={`${CLASE_INPUT} font-mono`}
          >
            {CATEGORIAS.map((c) => (
              <option key={c.valor} value={c.valor}>
                {c.etiqueta}
              </option>
            ))}
          </select>
        </Field>

        <div className="md:col-span-2">
          <Button type="submit" variant="ghost" className="w-full text-xs" disabled={guardando}>
            {guardando ? 'Guardando...' : '+ Agregar especie'}
          </Button>
        </div>
      </form>
    </Card>
  );
}
