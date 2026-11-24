'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Container } from '@/components/ui/Container';
import { BioHubUser } from '@/lib/permissions';

export interface SiteHeaderProps {
  user?: BioHubUser | null;
  onLogout?: () => void;
  showPanelNav?: boolean;
}

const THEME_STORAGE_KEY = 'biohub_theme_preference_v1';

export function SiteHeader({
  user = null,
  onLogout,
  showPanelNav = false,
}: SiteHeaderProps) {
  const [darkMode, setDarkMode] = useState<boolean>(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);
  const [userMenuOpen, setUserMenuOpen] = useState<boolean>(false);

  useEffect(() => {
    const root = document.documentElement;
    const saved = window.localStorage.getItem(THEME_STORAGE_KEY);
    if (saved === 'dark' || saved === 'light') {
      const isDark = saved === 'dark';
      setDarkMode(isDark);
      root.classList.toggle('dark', isDark);
    } else {
      const prefersDark = window.matchMedia(
        '(prefers-color-scheme: dark)'
      ).matches;
      setDarkMode(prefersDark);
      root.classList.toggle('dark', prefersDark);
    }
  }, []);

  const toggleTheme = () => {
    const next = !darkMode;
    setDarkMode(next);
    document.documentElement.classList.toggle('dark', next);
    try {
      window.localStorage.setItem(THEME_STORAGE_KEY, next ? 'dark' : 'light');
    } catch {
      // ignore storage error
    }
  };

  const navLinks = showPanelNav
    ? [
        { href: '#estacion-campo', label: 'Campo' },
        { href: '#inventario', label: 'Inventario' },
        { href: '#trazabilidad', label: 'Trazabilidad' },
        { href: '#operacion', label: 'API' },
        // Solo para quien administra cuentas (permiso roles:administrar)
        ...(user?.permisos.includes('roles:administrar')
          ? [{ href: '#administracion', label: 'Usuarios' }]
          : []),
      ]
    : [{ href: '#desafios', label: 'Desafíos científicos' }];

  return (
    <header className="sticky top-0 z-40 border-b border-forest-700/40 bg-forest-900 text-white">
      <Container className="flex h-16 items-center justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0">
          <Link
            href="/"
            className="font-serif text-2xl font-semibold tracking-tight text-white whitespace-nowrap focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 rounded-sm"
          >
            BioHub
          </Link>
          <span className="hidden lg:inline-block font-mono text-xs text-white/75 border-l border-white/20 pl-3 whitespace-nowrap">
            Instituto Nacional de Biodiversidad
          </span>
        </div>

        {/* Navegación de escritorio con etiquetas cortas y whitespace-nowrap */}
        <nav
          aria-label="Navegación principal"
          className="hidden md:flex items-center gap-6 text-sm text-white/90"
        >
          {navLinks.map((link) => (
            <a
              key={link.href}
              href={link.href}
              className="whitespace-nowrap py-2 hover:text-white transition-colors duration-150 focus-visible:outline-none focus-visible:underline"
            >
              {link.label}
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-2">
          {/* Botón de modo oscuro con ícono sol/luna y objetivo táctil >= 44px */}
          <button
            type="button"
            onClick={toggleTheme}
            aria-pressed={darkMode}
            aria-label={
              darkMode
                ? 'Cambiar a tema claro'
                : 'Cambiar a tema oscuro'
            }
            title={
              darkMode
                ? 'Cambiar a tema claro'
                : 'Cambiar a tema oscuro'
            }
            className="inline-flex min-h-11 min-w-11 items-center justify-center rounded-md border border-white/25 px-3 text-xs text-white hover:bg-forest-700 transition-colors duration-150 cursor-pointer"
          >
            {darkMode ? (
              <svg
                className="h-4 w-4 text-sun-500"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <circle cx="12" cy="12" r="4" />
                <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41" />
              </svg>
            ) : (
              <svg
                className="h-4 w-4 text-white"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
            )}
          </button>

          {/* Usuario autenticado como iniciales con menú o botón Iniciar sesión */}
          {user ? (
            <div className="relative">
              <button
                type="button"
                onClick={() => setUserMenuOpen((prev) => !prev)}
                aria-expanded={userMenuOpen}
                aria-haspopup="menu"
                aria-label={`Cuenta de ${user.name} (${user.roleLabel})`}
                className="inline-flex min-h-11 items-center gap-2 rounded-md border border-white/25 bg-forest-700 px-3 py-1.5 text-xs font-medium text-white hover:bg-moss-500 hover:text-[#1B2A22] transition-colors duration-150 cursor-pointer whitespace-nowrap"
              >
                <span className="flex h-6 w-6 items-center justify-center rounded-full bg-sun-500 font-mono text-[11px] font-bold text-[#1B2A22]">
                  {user.initials}
                </span>
                <span className="hidden sm:inline">{user.roleLabel}</span>
              </button>

              {userMenuOpen && (
                <div
                  role="menu"
                  aria-label="Opciones de sesión"
                  className="absolute right-0 mt-2 w-64 rounded-xl border border-line bg-surface p-3 text-ink shadow-sm z-50 space-y-2"
                >
                  <div className="border-b border-line pb-2">
                    <p className="font-serif text-sm font-semibold text-ink">
                      {user.name}
                    </p>
                    <p className="text-xs text-ink-muted">{user.roleLabel}</p>
                    <p className="font-mono text-[11px] text-ink-muted mt-0.5">
                      {user.station}
                    </p>
                  </div>
                  <div className="flex flex-col gap-1">
                    <Link
                      href="/login"
                      role="menuitem"
                      onClick={() => setUserMenuOpen(false)}
                      className="flex min-h-11 items-center rounded-md px-3 text-xs font-medium text-ink hover:bg-paper"
                    >
                      Cambiar perfil de acceso
                    </Link>
                    {onLogout && (
                      <button
                        type="button"
                        role="menuitem"
                        onClick={() => {
                          setUserMenuOpen(false);
                          onLogout();
                        }}
                        className="flex min-h-11 w-full items-center rounded-md px-3 text-left text-xs font-medium text-iucn-cr hover:bg-paper cursor-pointer"
                      >
                        Cerrar sesión
                      </button>
                    )}
                  </div>
                </div>
              )}
            </div>
          ) : (
            <Link
              href="/login"
              className="inline-flex min-h-11 items-center justify-center rounded-md bg-sun-500 px-4 py-2 text-xs font-semibold text-[#1B2A22] hover:brightness-95 transition-colors duration-150 whitespace-nowrap"
            >
              Iniciar sesión
            </Link>
          )}

          {/* Botón de menú móvil (hamburguesa) con objetivo táctil >= 44px */}
          <button
            type="button"
            onClick={() => setMobileMenuOpen((prev) => !prev)}
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-navigation-menu"
            aria-label={
              mobileMenuOpen ? 'Cerrar menú de navegación' : 'Abrir menú de navegación'
            }
            className="inline-flex min-h-11 min-w-11 items-center justify-center rounded-md border border-white/25 px-2.5 text-white hover:bg-forest-700 md:hidden cursor-pointer"
          >
            {mobileMenuOpen ? (
              <svg
                className="h-5 w-5"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                aria-hidden="true"
              >
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
            ) : (
              <svg
                className="h-5 w-5"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                aria-hidden="true"
              >
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            )}
          </button>
        </div>
      </Container>

      {/* Menú desplegable en móvil */}
      {mobileMenuOpen && (
        <nav
          id="mobile-navigation-menu"
          aria-label="Navegación móvil"
          className="border-t border-forest-700/50 bg-forest-900 px-4 py-3 md:hidden"
        >
          <ul className="flex flex-col gap-1">
            {navLinks.map((link) => (
              <li key={link.href}>
                <a
                  href={link.href}
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex min-h-11 items-center rounded-md px-3 text-sm text-white/95 hover:bg-forest-700"
                >
                  {link.label}
                </a>
              </li>
            ))}
            {user ? (
              <li>
                <Link
                  href="/panel"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex min-h-11 items-center rounded-md px-3 text-sm font-medium text-sun-500 hover:bg-forest-700"
                >
                  Ir al panel científico
                </Link>
              </li>
            ) : (
              <li>
                <Link
                  href="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex min-h-11 items-center rounded-md px-3 text-sm font-medium text-sun-500 hover:bg-forest-700"
                >
                  Iniciar sesión institucional
                </Link>
              </li>
            )}
          </ul>
        </nav>
      )}
    </header>
  );
}
