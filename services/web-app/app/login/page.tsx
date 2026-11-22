'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { Section } from '@/components/ui/Section';
import { Container } from '@/components/ui/Container';
import { SiteHeader } from '@/components/biohub/SiteHeader';
import {
  BioHubUser,
  DEMO_USERS,
  getStoredSession,
  saveStoredSession,
  clearStoredSession,
} from '@/lib/permissions';

export default function LoginPage() {
  const router = useRouter();
  const [currentUser, setCurrentUser] = useState<BioHubUser | null>(null);
  const [activeTab, setActiveTab] = useState<'perfiles' | 'credenciales'>('perfiles');
  const [selectedRoleId, setSelectedRoleId] = useState<string>(DEMO_USERS[0].id);
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    setCurrentUser(getStoredSession());
  }, []);

  const handleSelectProfile = (user: BioHubUser) => {
    saveStoredSession(user);
    setCurrentUser(user);
    router.push('/panel');
  };

  const handleCredentialSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    const found = DEMO_USERS.find(
      (u) => u.email.toLowerCase() === email.trim().toLowerCase()
    );
    if (!found) {
      setErrorMsg(
        'No se encontró una cuenta institucional asociada a ese correo. En el entorno de demostración puedes elegir un perfil directamente en la pestaña "Perfiles de demostración".'
      );
      return;
    }
    if (found.role !== 'public' && !password.trim()) {
      setErrorMsg('Ingresa tu clave de estación para continuar.');
      return;
    }

    saveStoredSession(found);
    setCurrentUser(found);
    router.push('/panel');
  };

  return (
    <div className="min-h-screen flex flex-col bg-paper text-ink">
      <SiteHeader
        user={currentUser}
        onLogout={() => {
          clearStoredSession();
          setCurrentUser(null);
        }}
        showPanelNav={false}
      />

      <main className="flex-1">
        <Section aria-labelledby="login-heading">
          <div className="mx-auto max-w-xl space-y-6">
            <div className="space-y-2">
              <div className="inline-flex items-center gap-2 rounded-md border border-sun-500/60 bg-sun-500/15 px-3 py-1 font-mono text-xs font-semibold text-ink">
                <span>Modo demo · Servicio de autorización en desarrollo</span>
              </div>
              <h1
                id="login-heading"
                className="font-serif text-3xl font-semibold text-ink"
              >
                Iniciar sesión en BioHub
              </h1>
              <p className="text-sm text-ink-muted">
                Selecciona tu perfil institucional para acceder a la estación de campo, el
                inventario taxonómico y la auditoría del corredor biológico.
              </p>
            </div>

            <Card className="space-y-6">
              {/* Pestañas accesibles con role="tablist" y role="tab" */}
              <div
                role="tablist"
                aria-label="Método de acceso"
                className="flex flex-wrap gap-2 border-b border-line pb-3"
              >
                <button
                  type="button"
                  role="tab"
                  aria-selected={activeTab === 'perfiles'}
                  onClick={() => {
                    setActiveTab('perfiles');
                    setErrorMsg(null);
                  }}
                  className={`min-h-11 rounded-md px-4 py-2 font-mono text-xs font-medium transition-colors cursor-pointer ${
                    activeTab === 'perfiles'
                      ? 'bg-forest-900 text-white'
                      : 'bg-paper text-ink border border-line hover:border-forest-700'
                  }`}
                >
                  Perfiles de demostración
                </button>
                <button
                  type="button"
                  role="tab"
                  aria-selected={activeTab === 'credenciales'}
                  onClick={() => {
                    setActiveTab('credenciales');
                    setErrorMsg(null);
                  }}
                  className={`min-h-11 rounded-md px-4 py-2 font-mono text-xs font-medium transition-colors cursor-pointer ${
                    activeTab === 'credenciales'
                      ? 'bg-forest-900 text-white'
                      : 'bg-paper text-ink border border-line hover:border-forest-700'
                  }`}
                >
                  Correo institucional
                </button>
              </div>

              {activeTab === 'perfiles' && (
                <div className="space-y-4">
                  <p className="text-xs text-ink-muted">
                    Elige el rol con el que deseas ingresar al panel científico. Tu sesión
                    quedará guardada en este navegador:
                  </p>

                  <div
                    role="radiogroup"
                    aria-label="Seleccionar perfil de usuario"
                    className="space-y-2.5"
                  >
                    {DEMO_USERS.map((u) => {
                      const selected = selectedRoleId === u.id;
                      return (
                        <button
                          key={u.id}
                          type="button"
                          role="radio"
                          aria-checked={selected}
                          onClick={() => setSelectedRoleId(u.id)}
                          className={`w-full text-left rounded-lg border p-3.5 transition-colors cursor-pointer ${
                            selected
                              ? 'border-forest-700 bg-paper ring-2 ring-moss-500/40'
                              : 'border-line bg-surface hover:bg-paper'
                          }`}
                        >
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <span className="font-serif text-base font-semibold text-ink">
                              {u.name}
                            </span>
                            <span className="font-mono text-xs px-2.5 py-0.5 rounded bg-forest-900 text-white">
                              {u.roleLabel}
                            </span>
                          </div>
                          <p className="font-mono text-xs text-ink-muted mt-1">
                            {u.station} · {u.institution}
                          </p>
                        </button>
                      );
                    })}
                  </div>

                  <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
                    <Link
                      href="/"
                      className="inline-flex min-h-11 items-center text-xs font-medium text-ink-muted hover:text-ink"
                    >
                      ← Volver a la presentación
                    </Link>
                    <Button
                      variant="primary"
                      onClick={() => {
                        const chosen =
                          DEMO_USERS.find((u) => u.id === selectedRoleId) ||
                          DEMO_USERS[0];
                        handleSelectProfile(chosen);
                      }}
                    >
                      Continuar al panel científico
                    </Button>
                  </div>
                </div>
              )}

              {activeTab === 'credenciales' && (
                <form onSubmit={handleCredentialSubmit} className="space-y-4">
                  <div aria-live="polite">
                    {errorMsg && (
                      <div
                        role="alert"
                        className="rounded-md border border-iucn-cr bg-iucn-cr/10 p-3 text-xs text-ink"
                      >
                        {errorMsg}
                      </div>
                    )}
                  </div>

                  <Field id="login-email" label="Correo electrónico institucional">
                    <input
                      id="login-email"
                      type="email"
                      required
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="nombre@biohub.gov.co"
                      className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-sm text-ink focus:outline-none focus:ring-2 focus:ring-moss-500"
                    />
                  </Field>

                  <Field id="login-password" label="Contraseña">
                    <input
                      id="login-password"
                      type="password"
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Ingresa tu contraseña"
                      className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-sm text-ink focus:outline-none focus:ring-2 focus:ring-moss-500"
                    />
                  </Field>

                  <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
                    <Link
                      href="/"
                      className="inline-flex min-h-11 items-center text-xs font-medium text-ink-muted hover:text-ink"
                    >
                      ← Volver a la presentación
                    </Link>
                    <Button type="submit" variant="primary">
                      Iniciar sesión
                    </Button>
                  </div>
                </form>
              )}
            </Card>
          </div>
        </Section>
      </main>

      <footer className="bg-forest-900 text-white border-t border-forest-700/40 py-10">
        <Container className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <p className="font-serif text-lg font-semibold text-white">BioHub</p>
          <p className="font-mono text-xs text-white/70">
            Instituto Nacional de Biodiversidad · Acceso controlado
          </p>
        </Container>
      </footer>
    </div>
  );
}
