'use client';

import React, { useCallback, useEffect, useState } from 'react';
import { CambioDeRol } from '@/components/biohub/CambioDeRol';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { ListaDesplazable } from '@/components/ui/ListaDesplazable';
import { useNotificar } from '@/components/ui/Notificaciones';
import { avisoDeError } from '@/lib/avisos';
import { tituloDeDia } from '@/lib/fechas';
import { BioHubUser, etiquetaRoles } from '@/lib/permissions';
import {
  AccionRol,
  cambiarRol,
  CambioRol,
  crearUsuario,
  historialRoles,
  listarRoles,
  listarUsuarios,
  VistaRol,
  VistaUsuario,
} from '@/lib/autorizacion-api';

const ETIQUETA_ACCION: Record<AccionRol, string> = {
  ROL_ASIGNADO: 'Rol asignado',
  ROL_QUITADO: 'Rol quitado',
  ROL_CREADO: 'Rol creado',
  PERMISOS_CAMBIADOS: 'Permisos del rol cambiados',
};

const claveDeCambio = (c: CambioRol) => String(c.id);
const diaDeCambio = (c: CambioRol) => tituloDeDia(c.realizadoEn);

function fechaHora(iso: string): string {
  return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
}

export interface AdminUsuariosProps {
  /** Usuario con el permiso roles:administrar. */
  admin: BioHubUser;
}

const CLASE_INPUT =
  'min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-sm text-ink focus:outline-none focus:ring-2 focus:ring-moss-500';

/**
 * Registro de cuentas nuevas con su rol. Todo lo valida servicio-autorizacion: id y correo únicos,
 * contraseña de 8 a 72 caracteres (se guarda solo como hash BCrypt) y que quien registra sea administrador.
 */
export function AdminUsuarios({ admin }: AdminUsuariosProps) {
  const [roles, setRoles] = useState<VistaRol[]>([]);
  const [usuarios, setUsuarios] = useState<VistaUsuario[]>([]);
  const [cargando, setCargando] = useState<boolean>(true);
  const [guardando, setGuardando] = useState<boolean>(false);
  const notificar = useNotificar();

  const [id, setId] = useState<string>('');
  const [nombre, setNombre] = useState<string>('');
  const [correo, setCorreo] = useState<string>('');
  const [clave, setClave] = useState<string>('');
  const [rol, setRol] = useState<string>('INVESTIGADOR');

  // Cambios de roles de las cuentas existentes
  const [historial, setHistorial] = useState<CambioRol[]>([]);
  const [cambiando, setCambiando] = useState<string | null>(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      const [listaRoles, listaUsuarios, cambios] = await Promise.all([
        listarRoles(),
        listarUsuarios(),
        historialRoles(),
      ]);
      setRoles(listaRoles);
      setUsuarios(listaUsuarios);
      setHistorial(cambios);
    } catch (error) {
      notificar(
        avisoDeError(error, {
          titulo: 'No se cargaron las cuentas',
          sinConexion: 'No se pudo leer la lista de cuentas',
          accion: 'administrar cuentas',
        })
      );
    } finally {
      setCargando(false);
    }
  }, [notificar]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const handleCrear = async (e: React.FormEvent) => {
    e.preventDefault();
    setGuardando(true);
    try {
      const creado = await crearUsuario({
        id: id.trim(),
        nombre: nombre.trim(),
        correo: correo.trim(),
        clave,
        rol: rol || null,
      });
      notificar({
        tipo: 'ok',
        titulo: 'Cuenta creada',
        detalle: `${creado.nombre} puede entrar como ${etiquetaRoles(creado.roles)}.`,
      });
      setId('');
      setNombre('');
      setCorreo('');
      setClave('');
      await cargar();
    } catch (error) {
      notificar(
        avisoDeError(error, {
          titulo: 'No se creó la cuenta',
          sinConexion: 'La cuenta no se guardó',
          accion: 'crear cuentas',
        })
      );
    } finally {
      setGuardando(false);
    }
  };

  const nombreDe = (usuarioId: string | null) =>
    usuarioId ? usuarios.find((u) => u.id === usuarioId)?.nombre ?? usuarioId : null;

  const handleCambiarRol = async (u: VistaUsuario, nuevo: string): Promise<boolean> => {
    setCambiando(u.id);
    let guardado = false;
    try {
      await cambiarRol(u.id, nuevo);
      notificar({
        tipo: 'ok',
        titulo: 'Rol cambiado',
        detalle: `${u.nombre} ahora es ${etiquetaRoles([nuevo])}.`,
      });
      guardado = true;
    } catch (error) {
      notificar(
        avisoDeError(error, {
          titulo: 'No se cambió el rol',
          sinConexion: 'El cambio de rol no se completó',
          accion: 'cambiar roles',
        })
      );
    } finally {
      // También si falló: así la lista muestra el rol que quedó guardado de verdad
      await cargar();
      setCambiando(null);
    }
    return guardado;
  };

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <Card as="section" className="lg:col-span-5 space-y-4">
          <div className="border-b border-line pb-3">
            <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
              Nueva cuenta institucional
            </span>
            <h3 className="mt-1 font-serif text-xl font-semibold text-ink">Registrar usuario</h3>
          </div>

          <form onSubmit={handleCrear} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <Field id="nu-id" label="Usuario" hint="Letras, números, punto o guion">
                <input
                  id="nu-id"
                  type="text"
                  required
                  autoComplete="off"
                  value={id}
                  onChange={(e) => setId(e.target.value)}
                  placeholder="maria.lopez"
                  className={`${CLASE_INPUT} font-mono`}
                />
              </Field>
              <Field id="nu-nombre" label="Nombre completo">
                <input
                  id="nu-nombre"
                  type="text"
                  required
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  placeholder="María López"
                  className={CLASE_INPUT}
                />
              </Field>
            </div>

            <Field id="nu-correo" label="Correo institucional">
              <input
                id="nu-correo"
                type="email"
                required
                autoComplete="off"
                value={correo}
                onChange={(e) => setCorreo(e.target.value)}
                placeholder="nombre@biohub.gov.co"
                className={`${CLASE_INPUT} font-mono`}
              />
            </Field>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <Field id="nu-clave" label="Contraseña inicial" hint="Mínimo 8 caracteres">
                <input
                  id="nu-clave"
                  type="password"
                  required
                  minLength={8}
                  maxLength={72}
                  autoComplete="new-password"
                  value={clave}
                  onChange={(e) => setClave(e.target.value)}
                  className={`${CLASE_INPUT} font-mono`}
                />
              </Field>
              <Field id="nu-rol" label="Rol">
                <select
                  id="nu-rol"
                  value={rol}
                  onChange={(e) => setRol(e.target.value)}
                  className={CLASE_INPUT}
                >
                  {roles.map((r) => (
                    <option key={r.nombre} value={r.nombre}>
                      {etiquetaRoles([r.nombre])}
                    </option>
                  ))}
                  <option value="">Sin rol (visitante común)</option>
                </select>
              </Field>
            </div>

            <Button type="submit" variant="primary" disabled={guardando || cargando} className="w-full">
              {guardando ? 'Registrando…' : 'Registrar usuario'}
            </Button>
          </form>
        </Card>

        <Card as="section" className="lg:col-span-7 space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line pb-3">
            <div>
              <span className="font-mono text-xs uppercase tracking-wider text-ink-muted">
                Servicio de autorización
              </span>
              <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
                Cuentas registradas ({usuarios.length})
              </h3>
            </div>
          </div>

          {cargando && usuarios.length === 0 ? (
            <p className="text-sm text-ink-muted">Cargando cuentas…</p>
          ) : usuarios.length === 0 ? (
            <p className="text-sm text-ink-muted">No hay cuentas registradas.</p>
          ) : (
            <ul className="divide-y divide-line">
              {usuarios.map((u) => (
                <li
                  key={u.id}
                  className="flex flex-col gap-3 py-4 sm:flex-row sm:items-start sm:justify-between"
                >
                  <div className="min-w-0">
                    <p className="font-serif text-base font-semibold text-ink">
                      {u.nombre}
                      {!u.activo && (
                        <span className="ml-2 rounded border border-line px-2 py-0.5 font-mono text-xs font-normal text-ink-muted">
                          Inactiva
                        </span>
                      )}
                    </p>
                    <p className="font-mono text-xs text-ink-muted break-all">{u.id}</p>
                    <p className="font-mono text-xs text-ink-muted break-all">{u.correo}</p>
                  </div>
                  <div className="min-w-0 sm:w-72 sm:shrink-0">
                    <CambioDeRol
                      usuario={u}
                      roles={roles}
                      // Nadie se quita a sí mismo el rol de administrador: lo rechaza servicio-autorizacion
                      bloqueado={u.id === admin.id && u.roles.includes('ADMIN')}
                      ocupado={cambiando !== null}
                      onCambiar={(rol) => handleCambiarRol(u, rol)}
                    />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>

      <Card as="section" aria-labelledby="historial-roles-heading" className="space-y-4">
        <div className="border-b border-line pb-3">
          <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
            Trazabilidad de accesos
          </span>
          <h3 id="historial-roles-heading" className="mt-1 font-serif text-xl font-semibold text-ink">
            Historial de cambios de roles ({historial.length})
          </h3>
          <p className="text-xs text-ink-muted">
            Cada rol asignado o quitado, y cada rol creado o con permisos cambiados, con quién lo hizo y cuándo. No
            se puede modificar ni borrar.
          </p>
        </div>
        {historial.length === 0 ? (
          <p className="text-sm text-ink-muted">Todavía no hay cambios de roles registrados.</p>
        ) : (
          <ListaDesplazable
            items={historial}
            etiqueta="Historial de cambios de roles"
            clave={claveDeCambio}
            grupo={diaDeCambio}
            className="max-h-96"
          >
            {(c) => (
              <div className="rounded-md border border-line bg-paper px-3 py-2 text-xs">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <span
                    className={`rounded px-2 py-0.5 font-mono text-[11px] font-semibold text-ink ${
                      c.accion === 'ROL_QUITADO' ? 'bg-iucn-cr/15' : 'bg-iucn-lc/15'
                    }`}
                  >
                    {ETIQUETA_ACCION[c.accion] ?? c.accion}
                  </span>
                  <span className="font-mono text-[11px] text-ink-muted">{fechaHora(c.realizadoEn)}</span>
                </div>
                <p className="mt-1 text-ink">
                  {c.usuarioId ? (
                    <>
                      <span className="font-semibold">{nombreDe(c.usuarioId)}</span> · {etiquetaRoles([c.rol])}
                    </>
                  ) : (
                    <>Rol {etiquetaRoles([c.rol])}</>
                  )}
                </p>
                {c.detalle && <p className="font-mono text-[11px] text-ink-muted break-all">{c.detalle}</p>}
                <p className="font-mono text-[11px] text-ink-muted">
                  Por {c.realizadoPor === 'sin registro' ? 'autor no registrado (cambio anterior al historial)' : c.realizadoPor}
                </p>
              </div>
            )}
          </ListaDesplazable>
        )}
      </Card>
    </div>
  );
}
