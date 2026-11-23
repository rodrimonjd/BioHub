'use client';

import React, { useCallback, useEffect, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { BioHubUser, etiquetaRoles } from '@/lib/permissions';
import {
  crearUsuario,
  ErrorAdministracion,
  listarRoles,
  listarUsuarios,
  VistaRol,
  VistaUsuario,
} from '@/lib/autorizacion-api';

export interface AdminUsuariosProps {
  /** Usuario con el permiso roles:administrar: sus peticiones van con su X-Usuario-Id. */
  admin: BioHubUser;
}

const CLASE_INPUT =
  'min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-sm text-ink focus:outline-none focus:ring-2 focus:ring-moss-500';

type Aviso = { tipo: 'ok' | 'err'; texto: string } | null;

/**
 * Registro de cuentas nuevas con su rol. Todo lo valida servicio-autorizacion: id y correo únicos,
 * contraseña de 8 a 72 caracteres (se guarda solo como hash BCrypt) y que quien registra sea administrador.
 */
export function AdminUsuarios({ admin }: AdminUsuariosProps) {
  const [roles, setRoles] = useState<VistaRol[]>([]);
  const [usuarios, setUsuarios] = useState<VistaUsuario[]>([]);
  const [cargando, setCargando] = useState<boolean>(true);
  const [guardando, setGuardando] = useState<boolean>(false);
  const [aviso, setAviso] = useState<Aviso>(null);

  const [id, setId] = useState<string>('');
  const [nombre, setNombre] = useState<string>('');
  const [correo, setCorreo] = useState<string>('');
  const [clave, setClave] = useState<string>('');
  const [rol, setRol] = useState<string>('INVESTIGADOR');

  const cargar = useCallback(async () => {
    setCargando(true);
    try {
      const [listaRoles, listaUsuarios] = await Promise.all([
        listarRoles(admin.id),
        listarUsuarios(admin.id),
      ]);
      setRoles(listaRoles);
      setUsuarios(listaUsuarios);
    } catch (error) {
      setAviso({
        tipo: 'err',
        texto: error instanceof ErrorAdministracion ? error.message : 'No se pudo cargar la lista de cuentas.',
      });
    } finally {
      setCargando(false);
    }
  }, [admin.id]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const handleCrear = async (e: React.FormEvent) => {
    e.preventDefault();
    setAviso(null);
    setGuardando(true);
    try {
      const creado = await crearUsuario(admin.id, {
        id: id.trim(),
        nombre: nombre.trim(),
        correo: correo.trim(),
        clave,
        rol: rol || null,
      });
      setAviso({
        tipo: 'ok',
        texto: `Cuenta "${creado.id}" registrada como ${etiquetaRoles(creado.roles)}.`,
      });
      setId('');
      setNombre('');
      setCorreo('');
      setClave('');
      await cargar();
    } catch (error) {
      setAviso({
        tipo: 'err',
        texto: error instanceof ErrorAdministracion ? error.message : 'No se pudo registrar la cuenta.',
      });
    } finally {
      setGuardando(false);
    }
  };

  return (
    <div className="space-y-4">
      <div aria-live="polite">
        {aviso && (
          <div
            role="status"
            className={`rounded-lg border p-4 text-xs font-mono ${
              aviso.tipo === 'ok'
                ? 'border-iucn-lc bg-iucn-lc/10 text-ink'
                : 'border-iucn-cr bg-iucn-cr/10 text-ink'
            }`}
          >
            {aviso.texto}
          </div>
        )}
      </div>

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

          {cargando ? (
            <p className="text-sm text-ink-muted">Cargando cuentas…</p>
          ) : usuarios.length === 0 ? (
            <p className="text-sm text-ink-muted">No hay cuentas registradas.</p>
          ) : (
            <ul className="divide-y divide-line">
              {usuarios.map((u) => (
                <li key={u.id} className="flex flex-wrap items-center justify-between gap-2 py-3">
                  <div className="min-w-0">
                    <p className="font-serif text-base font-semibold text-ink">{u.nombre}</p>
                    <p className="font-mono text-xs text-ink-muted break-all">
                      {u.id} · {u.correo}
                    </p>
                  </div>
                  <div className="flex flex-wrap items-center gap-1.5">
                    {u.roles.map((r) => (
                      <span
                        key={r}
                        className="font-mono text-xs px-2.5 py-0.5 rounded bg-forest-900 text-white"
                      >
                        {etiquetaRoles([r])}
                      </span>
                    ))}
                    {!u.activo && (
                      <span className="font-mono text-xs px-2.5 py-0.5 rounded border border-line text-ink-muted">
                        Inactiva
                      </span>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </div>
  );
}
