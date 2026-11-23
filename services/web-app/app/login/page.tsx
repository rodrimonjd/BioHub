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
  crearSesion,
  getStoredSession,
  saveStoredSession,
  clearStoredSession,
} from '@/lib/permissions';
import { ErrorSesion, iniciarSesion } from '@/lib/autorizacion-api';

export default function LoginPage() {
  const router = useRouter();
  const [currentUser, setCurrentUser] = useState<BioHubUser | null>(null);
  const [usuario, setUsuario] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [enviando, setEnviando] = useState<boolean>(false);

  useEffect(() => {
    setCurrentUser(getStoredSession());
  }, []);

  // Las credenciales se validan en servicio-autorizacion; el navegador nunca guarda la contraseña.
  const handleCredentialSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setEnviando(true);
    try {
      const sesion = crearSesion(await iniciarSesion(usuario.trim(), password));
      saveStoredSession(sesion);
      setCurrentUser(sesion);
      setPassword('');
      router.push('/panel');
    } catch (error) {
      setErrorMsg(
        error instanceof ErrorSesion
          ? error.message
          : 'No se pudo iniciar sesión. Inténtalo de nuevo.'
      );
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-paper text-ink">
      <SiteHeader
        user={currentUser}
        onLogout={() => {
          clearStoredSession();
          setCurrentUser(null);
          router.push('/');
        }}
        showPanelNav={false}
      />

      <main className="flex-1">
        <Section aria-labelledby="login-heading">
          <div className="mx-auto max-w-xl space-y-6">
            <div className="space-y-2">
              <h1
                id="login-heading"
                className="font-serif text-3xl font-semibold text-ink"
              >
                Iniciar sesión en BioHub
              </h1>
              <p className="text-sm text-ink-muted">
                Ingresa con tu cuenta institucional para acceder a la estación de campo, el
                inventario taxonómico y la auditoría del corredor biológico. Lo que puedes ver
                y hacer depende de los permisos de tu rol.
              </p>
            </div>

            <Card className="space-y-6">
              {currentUser && (
                <p className="rounded-md border border-line bg-paper p-3 text-xs text-ink-muted">
                  Ya tienes una sesión abierta como{' '}
                  <span className="font-medium text-ink">{currentUser.name}</span>.{' '}
                  <Link href="/panel" className="font-medium text-ink underline">
                    Ir al panel científico
                  </Link>
                </p>
              )}

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

                <Field id="login-usuario" label="Usuario o correo institucional">
                  <input
                    id="login-usuario"
                    type="text"
                    required
                    autoComplete="username"
                    value={usuario}
                    onChange={(e) => setUsuario(e.target.value)}
                    placeholder="usuario o nombre@biohub.gov.co"
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-sm text-ink focus:outline-none focus:ring-2 focus:ring-moss-500"
                  />
                </Field>

                <Field id="login-password" label="Contraseña">
                  <input
                    id="login-password"
                    type="password"
                    required
                    autoComplete="current-password"
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
                  <Button type="submit" variant="primary" disabled={enviando}>
                    {enviando ? 'Verificando…' : 'Iniciar sesión'}
                  </Button>
                </div>
              </form>
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
