'use client';

import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import {
  BioHubUser,
  canManageEquipment,
  PERMISOS,
} from '@/lib/permissions';
import { listarUsuariosConPermiso, ResumenUsuario } from '@/lib/autorizacion-api';
import {
  asignarEquipoAExpedicion,
  crearEquipo,
  crearExpedicion,
  Equipo,
  Expedicion,
  listarEquipos,
} from '@/lib/registro-api';
import { Aviso, CLASE_INPUT, Encabezado, MensajeAviso, mensajeDeError } from './campo-ui';

export interface GestionCampoProps {
  currentUser: BioHubUser;
  expediciones: Expedicion[];
  onExpedicionesCambiaron: () => Promise<void> | void;
}

const TIPOS_EQUIPO = ['GPS', 'Cámara trampa', 'Dron', 'Hidrófono', 'Sensor ambiental'];

const ZONAS = ['Vichada', 'Meta', 'Casanare', 'Amazonas', 'Caquetá', 'Chocó', 'Antioquia'];

export function GestionCampo({
  currentUser,
  expediciones,
  onExpedicionesCambiaron,
}: GestionCampoProps) {
  const puedeGestionarEquipos = canManageEquipment(currentUser);

  const [equipos, setEquipos] = useState<Equipo[]>([]);
  const [lideres, setLideres] = useState<ResumenUsuario[]>([]);

  const recargarEquipos = async () => setEquipos(await listarEquipos());

  useEffect(() => {
    recargarEquipos();
    listarUsuariosConPermiso(currentUser.id, PERMISOS.CREAR_HALLAZGO).then(setLideres);
  }, [currentUser.id]);

  // Equipo
  const [eqSerie, setEqSerie] = useState('');
  const [eqTipo, setEqTipo] = useState(TIPOS_EQUIPO[0]);
  const [eqModelo, setEqModelo] = useState('');
  const [avisoEquipo, setAvisoEquipo] = useState<Aviso>(null);

  const handleCrearEquipo = async (e: React.FormEvent) => {
    e.preventDefault();
    setAvisoEquipo(null);
    try {
      const creado = await crearEquipo(
        { numeroSerie: eqSerie.trim(), tipo: eqTipo, modelo: eqModelo.trim() || null },
        currentUser.id
      );
      setAvisoEquipo({ tipo: 'ok', texto: `Equipo ${creado.numeroSerie} registrado.` });
      setEqSerie('');
      setEqModelo('');
      await recargarEquipos();
    } catch (error) {
      setAvisoEquipo({ tipo: 'err', texto: mensajeDeError(error) });
    }
  };

  // Expedición
  const [exNombre, setExNombre] = useState('');
  const [exZona, setExZona] = useState(ZONAS[0]);
  const [exInicio, setExInicio] = useState(new Date().toISOString().slice(0, 10));
  const [exFin, setExFin] = useState('');
  const [exLider, setExLider] = useState(currentUser.id);
  const [avisoExpedicion, setAvisoExpedicion] = useState<Aviso>(null);

  const handleCrearExpedicion = async (e: React.FormEvent) => {
    e.preventDefault();
    setAvisoExpedicion(null);
    try {
      const creada = await crearExpedicion(
        {
          nombre: exNombre.trim(),
          zona: exZona,
          fechaInicio: exInicio,
          fechaFin: exFin || null,
          liderId: exLider,
        },
        currentUser.id
      );
      setAvisoExpedicion({ tipo: 'ok', texto: `Expedición "${creada.nombre}" creada.` });
      setExNombre('');
      setExFin('');
      await onExpedicionesCambiaron();
    } catch (error) {
      setAvisoExpedicion({ tipo: 'err', texto: mensajeDeError(error) });
    }
  };

  // Asignar equipo a expedición
  const [asExpedicionId, setAsExpedicionId] = useState('');
  const [asEquipoId, setAsEquipoId] = useState('');
  const [avisoAsignacion, setAvisoAsignacion] = useState<Aviso>(null);

  const handleAsignar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!asExpedicionId || !asEquipoId) return;
    setAvisoAsignacion(null);
    try {
      const expedicion = await asignarEquipoAExpedicion(asExpedicionId, asEquipoId, currentUser.id);
      setAvisoAsignacion({
        tipo: 'ok',
        texto: `Equipo asignado a "${expedicion.nombre}"; su responsable ahora es ${expedicion.liderId}.`,
      });
      await Promise.all([onExpedicionesCambiaron(), recargarEquipos()]);
    } catch (error) {
      setAvisoAsignacion({ tipo: 'err', texto: mensajeDeError(error) });
    }
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
        {puedeGestionarEquipos && (
          <Card className="space-y-4">
            <Encabezado
              titulo="Registrar equipo de campo"
              descripcion="Drones, cámaras trampa, hidrófonos y sensores. Al asignarlo a una expedición, su responsable pasa a ser el líder."
            />
            <MensajeAviso aviso={avisoEquipo} />
            <form onSubmit={handleCrearEquipo} className="grid grid-cols-1 sm:grid-cols-3 gap-3 items-end">
              <Field id="eq-serie" label="Número de serie">
                <input
                  id="eq-serie"
                  required
                  value={eqSerie}
                  onChange={(e) => setEqSerie(e.target.value)}
                  placeholder="GPS-0002"
                  className={`${CLASE_INPUT} font-mono`}
                />
              </Field>
              <Field id="eq-tipo" label="Tipo">
                <select id="eq-tipo" value={eqTipo} onChange={(e) => setEqTipo(e.target.value)} className={CLASE_INPUT}>
                  {TIPOS_EQUIPO.map((t) => (
                    <option key={t} value={t}>
                      {t}
                    </option>
                  ))}
                </select>
              </Field>
              <Field id="eq-modelo" label="Modelo (opcional)">
                <input
                  id="eq-modelo"
                  value={eqModelo}
                  onChange={(e) => setEqModelo(e.target.value)}
                  placeholder="Garmin 66i"
                  className={CLASE_INPUT}
                />
              </Field>
              <Button type="submit" variant="ghost" className="sm:col-span-3 text-xs">
                + Registrar equipo
              </Button>
            </form>

            <ul className="divide-y divide-line rounded-md border border-line font-mono text-xs">
              {equipos.length === 0 && <li className="px-3 py-2 text-ink-muted">Sin equipos registrados.</li>}
              {equipos.map((eq) => (
                <li key={eq.id} className="flex flex-wrap justify-between gap-2 px-3 py-2">
                  <span className="text-ink">
                    {eq.numeroSerie} · {eq.tipo}
                    {eq.modelo ? ` · ${eq.modelo}` : ''}
                  </span>
                  <span className="text-ink-muted">
                    {eq.estado} · {eq.responsableId ?? 'sin responsable'}
                  </span>
                </li>
              ))}
            </ul>
          </Card>
        )}

        {puedeGestionarEquipos && (
          <Card className="space-y-4">
            <Encabezado
              titulo="Crear expedición"
              descripcion="Agrupa los hallazgos de una salida de campo y los equipos que se llevan."
            />
            <MensajeAviso aviso={avisoExpedicion} />
            <form onSubmit={handleCrearExpedicion} className="grid grid-cols-1 sm:grid-cols-2 gap-3 items-end">
              <Field id="ex-nombre" label="Nombre" className="sm:col-span-2">
                <input
                  id="ex-nombre"
                  required
                  value={exNombre}
                  onChange={(e) => setExNombre(e.target.value)}
                  placeholder="Expedición Río Tomo 2026"
                  className={CLASE_INPUT}
                />
              </Field>
              <Field id="ex-zona" label="Zona">
                <select id="ex-zona" value={exZona} onChange={(e) => setExZona(e.target.value)} className={CLASE_INPUT}>
                  {ZONAS.map((z) => (
                    <option key={z} value={z}>
                      {z}
                    </option>
                  ))}
                </select>
              </Field>
              <Field id="ex-lider" label="Líder">
                <select id="ex-lider" value={exLider} onChange={(e) => setExLider(e.target.value)} className={CLASE_INPUT}>
                  {!lideres.some((l) => l.id === currentUser.id) && (
                    <option value={currentUser.id}>{currentUser.name}</option>
                  )}
                  {lideres.map((l) => (
                    <option key={l.id} value={l.id}>
                      {l.nombre}
                    </option>
                  ))}
                </select>
              </Field>
              <Field id="ex-inicio" label="Desde">
                <input
                  id="ex-inicio"
                  type="date"
                  required
                  value={exInicio}
                  onChange={(e) => setExInicio(e.target.value)}
                  className={CLASE_INPUT}
                />
              </Field>
              <Field id="ex-fin" label="Hasta (opcional)">
                <input
                  id="ex-fin"
                  type="date"
                  value={exFin}
                  onChange={(e) => setExFin(e.target.value)}
                  className={CLASE_INPUT}
                />
              </Field>
              <Button type="submit" variant="ghost" className="sm:col-span-2 text-xs">
                + Crear expedición
              </Button>
            </form>

            <form onSubmit={handleAsignar} className="space-y-3 border-t border-line pt-4">
              <p className="font-mono text-xs font-semibold uppercase tracking-wider text-moss-500">
                Asignar equipo a una expedición
              </p>
              <MensajeAviso aviso={avisoAsignacion} />
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <Field id="as-expedicion" label="Expedición">
                  <select
                    id="as-expedicion"
                    required
                    value={asExpedicionId}
                    onChange={(e) => setAsExpedicionId(e.target.value)}
                    className={CLASE_INPUT}
                  >
                    <option value="">Elegir...</option>
                    {expediciones.map((ex) => (
                      <option key={ex.id} value={ex.id}>
                        {ex.nombre} · {ex.zona} ({ex.equipos?.length ?? 0} equipos)
                      </option>
                    ))}
                  </select>
                </Field>
                <Field id="as-equipo" label="Equipo">
                  <select
                    id="as-equipo"
                    required
                    value={asEquipoId}
                    onChange={(e) => setAsEquipoId(e.target.value)}
                    className={CLASE_INPUT}
                  >
                    <option value="">Elegir...</option>
                    {equipos.map((eq) => (
                      <option key={eq.id} value={eq.id}>
                        {eq.numeroSerie} · {eq.tipo}
                      </option>
                    ))}
                  </select>
                </Field>
              </div>
              <Button type="submit" variant="ghost" className="w-full text-xs">
                Asignar equipo
              </Button>
            </form>
          </Card>
        )}
      </div>
    </div>
  );
}
