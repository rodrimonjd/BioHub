'use client';

import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { ReportStatus, BackgroundReportState } from '@/components/biohub/ReportStatus';
import { SeccionBloqueada } from '@/components/biohub/SeccionBloqueada';
import { BioHubUser, canGenerateReports } from '@/lib/permissions';
import {
  Reporte,
  descargarPdf,
  obtenerReporte,
  sigueEnProceso,
  solicitarReporte,
  ultimoReporteDe,
} from '@/lib/reportes-api';

const DEPARTAMENTOS = ['Vichada', 'Meta', 'Casanare', 'Amazonas', 'Caquetá', 'Chocó', 'Antioquia'];

// Cada cuánto se consulta el estado mientras el reporte está en cola o generando.
const INTERVALO_CONSULTA_MS = 2000;

const ESTADO_EN_TARJETA: Record<Reporte['estado'], BackgroundReportState> = {
  EN_COLA: 'en_cola',
  GENERANDO: 'generando',
  LISTO: 'listo',
  SIN_DATOS: 'sin_datos',
  ERROR: 'error',
};

function hoy(): string {
  return new Date().toISOString().slice(0, 10);
}

function haceUnMes(): string {
  const fecha = new Date();
  fecha.setMonth(fecha.getMonth() - 1);
  return fecha.toISOString().slice(0, 10);
}

export function ReportesPanel({ currentUser }: { currentUser: BioHubUser }) {
  const [fechaInicio, setFechaInicio] = useState<string>(haceUnMes());
  const [fechaFin, setFechaFin] = useState<string>(hoy());
  const [departamento, setDepartamento] = useState<string>('');
  const [reporte, setReporte] = useState<Reporte | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState<boolean>(false);

  const puedeGenerar = canGenerateReports(currentUser);

  // Al entrar se muestra el último reporte que pidió este usuario.
  useEffect(() => {
    ultimoReporteDe(currentUser.id).then(setReporte);
  }, [currentUser.id]);

  // Mientras el reporte siga en cola o generando, se consulta su estado.
  useEffect(() => {
    if (!reporte || !sigueEnProceso(reporte.estado)) return;
    const temporizador = setTimeout(async () => {
      try {
        setReporte(await obtenerReporte(reporte.id, currentUser.id));
      } catch (e) {
        setError(e instanceof Error ? e.message : 'No se pudo consultar el reporte');
      }
    }, INTERVALO_CONSULTA_MS);
    return () => clearTimeout(temporizador);
  }, [reporte, currentUser.id]);

  async function handleDescargar(id: string) {
    setError(null);
    try {
      await descargarPdf(id, currentUser.id);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo descargar el reporte');
    }
  }

  async function handleSolicitar(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    if (fechaInicio > fechaFin) {
      setError('La fecha de inicio no puede ser posterior a la fecha de fin.');
      return;
    }
    setEnviando(true);
    try {
      const nuevo = await solicitarReporte({
        fechaInicio,
        fechaFin,
        departamento: departamento || undefined,
        solicitadoPor: currentUser.id,
      });
      setReporte(nuevo);
    } catch (e) {
      setError(
        e instanceof Error && e.message !== 'Failed to fetch'
          ? e.message
          : 'No hay conexión con el servicio de reportes.'
      );
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="space-y-4">
      {puedeGenerar ? (
        <Card>
          <form onSubmit={handleSolicitar} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <Field id="reporte-inicio" label="Desde">
                <input
                  id="reporte-inicio"
                  type="date"
                  value={fechaInicio}
                  onChange={(e) => setFechaInicio(e.target.value)}
                  className="w-full rounded-md border border-line bg-paper px-3 py-2 text-sm text-ink"
                  required
                />
              </Field>
              <Field id="reporte-fin" label="Hasta">
                <input
                  id="reporte-fin"
                  type="date"
                  value={fechaFin}
                  onChange={(e) => setFechaFin(e.target.value)}
                  className="w-full rounded-md border border-line bg-paper px-3 py-2 text-sm text-ink"
                  required
                />
              </Field>
              <Field id="reporte-depto" label="Departamento">
                <select
                  id="reporte-depto"
                  value={departamento}
                  onChange={(e) => setDepartamento(e.target.value)}
                  className="w-full rounded-md border border-line bg-paper px-3 py-2 text-sm text-ink"
                >
                  <option value="">Todos</option>
                  {DEPARTAMENTOS.map((d) => (
                    <option key={d} value={d}>
                      {d}
                    </option>
                  ))}
                </select>
              </Field>
            </div>
            <Button type="submit" disabled={enviando || (reporte !== null && sigueEnProceso(reporte.estado))}>
              {enviando ? 'Solicitando...' : 'Generar reporte'}
            </Button>
            {error && (
              <p role="alert" className="rounded-md border border-iucn-cr/40 bg-iucn-cr/10 px-3 py-2 text-xs text-iucn-cr">
                {error}
              </p>
            )}
          </form>
        </Card>
      ) : (
        <SeccionBloqueada
          titulo="Generación de reportes bloqueada"
          descripcion="Requiere el permiso para solicitar reportes (investigador, autoridad ambiental o administrador)."
        />
      )}

      {!puedeGenerar && error && (
        <p role="alert" className="rounded-md border border-iucn-cr/40 bg-iucn-cr/10 px-3 py-2 text-xs text-iucn-cr">
          {error}
        </p>
      )}

      {reporte && (
        <ReportStatus
          reportTitle={`Índice de presión sobre ecosistemas · ${reporte.departamento ?? 'Todo el país'}`}
          reportId={reporte.id.slice(0, 8).toUpperCase()}
          recordsProcessed={`${reporte.totalAvistamientos} avistamientos · ${reporte.totalIndividuos} individuos`}
          state={ESTADO_EN_TARJETA[reporte.estado]}
          onDownload={() => handleDescargar(reporte.id)}
          message={reporte.estado === 'ERROR' ? reporte.mensaje : null}
        />
      )}
    </div>
  );
}
