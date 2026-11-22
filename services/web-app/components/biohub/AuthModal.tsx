'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { BioHubUser, DEMO_USERS } from '@/lib/permissions';

export interface AuthModalProps {
  isOpen: boolean;
  currentUser: BioHubUser;
  onClose: () => void;
  onSelectUser: (user: BioHubUser) => void;
}

export function AuthModal({
  isOpen,
  currentUser,
  onClose,
  onSelectUser,
}: AuthModalProps) {
  const [email, setEmail] = useState(DEMO_USERS[0].email);
  const [password, setPassword] = useState('biohub2026');
  const [apiKey, setApiKey] = useState('bh_live_unbosque_2026_key99');
  const [authTab, setAuthTab] = useState<'institucional' | 'perfiles' | 'externo'>(
    'institucional'
  );
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleCredentialLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    const found = DEMO_USERS.find(
      (u) => u.email.toLowerCase() === email.trim().toLowerCase()
    );
    if (!found) {
      setErrorMsg(
        'Credencial no encontrada. Usa uno de los correos institucionales (@biohub.gov.co) o selecciona un perfil rápido.'
      );
      return;
    }
    if (found.role !== 'public' && password.trim().length < 4) {
      setErrorMsg('Ingresa una contraseña válida (ej. biohub2026).');
      return;
    }
    onSelectUser(found);
    onClose();
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="auth-modal-title"
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/60 p-4 backdrop-blur-[2px]"
    >
      <Card className="w-full max-w-xl space-y-5 border border-line bg-surface p-6">
        <div className="flex items-start justify-between gap-4 border-b border-line pb-4">
          <div>
            <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
              Servicio de Autorización Centralizado · HU-08.2 / HU-11.1
            </p>
            <h2
              id="auth-modal-title"
              className="mt-1 font-serif text-2xl font-semibold text-ink"
            >
              Acceso Institucional BioHub
            </h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Cerrar ventana de acceso"
            className="rounded-md border border-line px-2.5 py-1 font-mono text-xs text-ink-muted hover:text-ink cursor-pointer"
          >
            ESC ✕
          </button>
        </div>

        {/* Selector de pestañas de autenticación */}
        <div className="flex flex-wrap gap-2 border-b border-line pb-3">
          <button
            type="button"
            onClick={() => setAuthTab('institucional')}
            className={`rounded-md px-3 py-1.5 font-mono text-xs transition-colors cursor-pointer ${
              authTab === 'institucional'
                ? 'bg-forest-900 text-white'
                : 'bg-paper text-ink border border-line'
            }`}
          >
            1. Correo institucional
          </button>
          <button
            type="button"
            onClick={() => setAuthTab('perfiles')}
            className={`rounded-md px-3 py-1.5 font-mono text-xs transition-colors cursor-pointer ${
              authTab === 'perfiles'
                ? 'bg-forest-900 text-white'
                : 'bg-paper text-ink border border-line'
            }`}
          >
            2. Cambio rápido de rol (Demo HUs)
          </button>
          <button
            type="button"
            onClick={() => setAuthTab('externo')}
            className={`rounded-md px-3 py-1.5 font-mono text-xs transition-colors cursor-pointer ${
              authTab === 'externo'
                ? 'bg-forest-900 text-white'
                : 'bg-paper text-ink border border-line'
            }`}
          >
            3. API Key Universidad / ONG
          </button>
        </div>

        {authTab === 'institucional' && (
          <form onSubmit={handleCredentialLogin} className="space-y-4">
            {errorMsg && (
              <div className="rounded-md border border-iucn-cr bg-iucn-cr/10 p-3 text-xs text-ink">
                {errorMsg}
              </div>
            )}

            <div>
              <label
                htmlFor="auth-email"
                className="block text-xs font-medium text-ink mb-1"
              >
                Correo electrónico institucional
              </label>
              <input
                id="auth-email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="sgarcia@biohub.gov.co"
                className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-sm text-ink focus:outline-none focus:ring-2 focus:ring-moss-500"
              />
            </div>

            <div>
              <label
                htmlFor="auth-password"
                className="block text-xs font-medium text-ink mb-1"
              >
                Contraseña de estación base
              </label>
              <input
                id="auth-password"
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-sm text-ink focus:outline-none focus:ring-2 focus:ring-moss-500"
              />
              <p className="mt-1 font-mono text-[11px] text-ink-muted">
                Cuentas activas: <code className="text-ink">sgarcia@biohub.gov.co</code> (Investigador),{' '}
                <code className="text-ink">eescobar@biohub.gov.co</code> (Alto Rango),{' '}
                <code className="text-ink">sbonilla@biohub.gov.co</code> (Auditor),{' '}
                <code className="text-ink">jdrodriguez@biohub.gov.co</code> (Director).
              </p>
            </div>

            <div className="flex items-center justify-end gap-2 pt-2">
              <Button variant="ghost" onClick={onClose}>
                Cancelar
              </Button>
              <Button type="submit" variant="primary">
                Iniciar sesión científica
              </Button>
            </div>
          </form>
        )}

        {authTab === 'perfiles' && (
          <div className="space-y-3">
            <p className="text-xs text-ink-muted">
              Selecciona cualquier actor del sistema para probar en vivo sus permisos sobre
              coordenadas sensibles (<code className="font-mono">HU-07</code>), registro de
              campo (<code className="font-mono">HU-01 / HU-13</code>) y reclasificación (
              <code className="font-mono">HU-10</code>):
            </p>
            <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
              {DEMO_USERS.map((u) => {
                const isSelected = currentUser.id === u.id;
                return (
                  <button
                    key={u.id}
                    type="button"
                    onClick={() => {
                      onSelectUser(u);
                      onClose();
                    }}
                    className={`w-full text-left rounded-lg border p-3 transition-colors cursor-pointer ${
                      isSelected
                        ? 'border-forest-700 bg-paper'
                        : 'border-line bg-surface hover:bg-paper'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-serif text-base font-semibold text-ink">
                        {u.name}
                      </span>
                      <span className="font-mono text-xs px-2 py-0.5 rounded bg-forest-900 text-white">
                        {u.roleLabel}
                      </span>
                    </div>
                    <p className="font-mono text-xs text-ink-muted mt-0.5">
                      ID: {u.id} · {u.email} · {u.station}
                    </p>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {authTab === 'externo' && (
          <div className="space-y-4">
            <p className="text-xs text-ink-muted leading-relaxed">
              Las universidades y ONGs se autentican mediante <code className="font-mono">X-API-Key</code>{' '}
              en el punto de entrada controlado (<code className="font-mono">HU-11.1</code> y{' '}
              <code className="font-mono">HU-16</code>) con límite de 60 solicitudes/minuto.
            </p>
            <div>
              <label className="block text-xs font-medium text-ink mb-1">
                Clave de Consumidor Externo (`X-API-Key`)
              </label>
              <input
                type="text"
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
              />
            </div>
            <div className="flex justify-end">
              <Button
                variant="secondary"
                onClick={() => {
                  onClose();
                  const el = document.getElementById('operacion');
                  el?.scrollIntoView({ behavior: 'smooth' });
                }}
              >
                Ir a consola de API Gateway
              </Button>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}
