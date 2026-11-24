'use client';

import React, { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import { AnimatePresence, motion, MotionConfig } from 'motion/react';
import { Container } from '@/components/ui/Container';
import {
  BioHubUser,
  canManageLocationAccess,
  canViewApiUsage,
  canViewReports,
  PERMISOS,
  tienePermiso,
} from '@/lib/permissions';
import { useSeccionActiva } from '@/lib/useSeccionActiva';

export interface SiteHeaderProps {
  user?: BioHubUser | null;
  onLogout?: () => void;
  showPanelNav?: boolean;
}

const THEME_STORAGE_KEY = 'biohub_theme_preference_v1';

interface EnlaceNav {
  id: string;
  label: string;
}

/** Secciones del panel en el mismo orden en que aparecen en la página; cada cuenta ve solo las suyas. */
function seccionesDelPanel(user: BioHubUser | null): EnlaceNav[] {
  return [
    { id: 'estacion-campo', label: 'Campo', visible: true },
    { id: 'administracion', label: 'Usuarios', visible: tienePermiso(user, PERMISOS.ADMINISTRAR_ROLES) },
    { id: 'inventario', label: 'Fichas', visible: true },
    { id: 'trazabilidad', label: 'Trazabilidad', visible: true },
    { id: 'accesos-ubicacion', label: 'Ubicaciones', visible: canManageLocationAccess(user) },
    { id: 'operacion', label: 'API', visible: canViewApiUsage(user) },
    { id: 'reportes', label: 'Reportes', visible: canViewReports(user) },
  ].filter((s) => s.visible);
}

const SECCIONES_PUBLICAS: EnlaceNav[] = [{ id: 'desafios', label: 'Desafíos científicos' }];

export function SiteHeader({
  user = null,
  onLogout,
  showPanelNav = false,
}: SiteHeaderProps) {
  const [darkMode, setDarkMode] = useState<boolean>(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);
  const [userMenuOpen, setUserMenuOpen] = useState<boolean>(false);
  const [compacto, setCompacto] = useState<boolean>(false);
  const menuCuenta = useRef<HTMLDivElement>(null);
  const botonCuenta = useRef<HTMLButtonElement>(null);
  const botonMenu = useRef<HTMLButtonElement>(null);

  const enlaces = showPanelNav ? seccionesDelPanel(user) : SECCIONES_PUBLICAS;
  const { activa, irA } = useSeccionActiva(enlaces.map((e) => e.id));

  // En páginas sin esas secciones (por ejemplo /login) el enlace lleva al inicio
  const hrefDe = (id: string) => (showPanelNav ? `#${id}` : `/#${id}`);
  const alElegir = (e: React.MouseEvent, id: string) => {
    if (e.metaKey || e.ctrlKey || e.shiftKey) return;
    if (irA(id)) e.preventDefault();
    setMobileMenuOpen(false);
  };

  useEffect(() => {
    // Dos umbrales: al encogerse el navbar la página se mueve unos píxeles y no debe volver a crecer
    const alBajar = () => setCompacto((prev) => window.scrollY > (prev ? 8 : 48));
    alBajar();
    window.addEventListener('scroll', alBajar, { passive: true });
    return () => window.removeEventListener('scroll', alBajar);
  }, []);

  // Los menús se cierran con Escape, y el de la cuenta también al hacer clic afuera
  useEffect(() => {
    if (!userMenuOpen && !mobileMenuOpen) return;
    const alPulsar = (e: KeyboardEvent) => {
      if (e.key !== 'Escape') return;
      (userMenuOpen ? botonCuenta : botonMenu).current?.focus();
      setUserMenuOpen(false);
      setMobileMenuOpen(false);
    };
    const alTocar = (e: PointerEvent) => {
      if (!menuCuenta.current?.contains(e.target as Node)) setUserMenuOpen(false);
    };
    document.addEventListener('keydown', alPulsar);
    document.addEventListener('pointerdown', alTocar);
    return () => {
      document.removeEventListener('keydown', alPulsar);
      document.removeEventListener('pointerdown', alTocar);
    };
  }, [userMenuOpen, mobileMenuOpen]);

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

  return (
    <MotionConfig reducedMotion="user">
    <header
      className={`sticky top-0 z-40 border-b text-white transition-[background-color,box-shadow,border-color] duration-300 ${
        compacto
          ? 'border-white/10 bg-forest-900/92 shadow-[0_10px_30px_-12px_rgba(0,0,0,0.55)] backdrop-blur-md'
          : 'border-forest-700/40 bg-forest-900'
      }`}
    >
      <Container
        className={`flex items-center justify-between gap-3 transition-[height] duration-300 ${compacto ? 'h-16' : 'h-20'}`}
      >
        <div className="flex items-center gap-3 min-w-0">
          <Link
            href="/"
            className="font-serif text-2xl font-semibold tracking-tight text-white whitespace-nowrap focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 rounded-sm"
          >
            BioHub
          </Link>
          <span
            className={`font-mono text-xs text-white/75 border-l border-white/20 pl-3 whitespace-nowrap ${
              showPanelNav ? 'hidden' : 'hidden xl:inline-block'
            }`}
          >
            Instituto Nacional de Biodiversidad
          </span>
        </div>

        {/* Navegación de escritorio: píldora con el relleno que sube y un punto en la sección activa */}
        <nav
          aria-label="Navegación principal"
          className="hidden lg:flex items-center rounded-full border border-white/15 bg-black/15 p-[3px] text-[13px] font-medium"
        >
          {enlaces.map((enlace) => {
            const esActiva = activa === enlace.id;
            return (
              <a
                key={enlace.id}
                href={hrefDe(enlace.id)}
                onClick={(e) => alElegir(e, enlace.id)}
                aria-current={esActiva ? 'true' : undefined}
                className={`group relative flex h-11 items-center overflow-hidden whitespace-nowrap rounded-full px-3.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 ${
                  esActiva ? 'text-sun-500' : 'text-white/85'
                }`}
              >
                <span
                  aria-hidden="true"
                  className="absolute left-1/2 top-full aspect-square w-[135%] -translate-x-1/2 rounded-full bg-sun-500 transition-transform duration-[420ms] ease-[cubic-bezier(0.22,1,0.36,1)] group-hover:-translate-y-[68%] group-focus-visible:-translate-y-[68%]"
                />
                <span className="relative block h-5 overflow-hidden leading-5">
                  <span className="block transition-transform duration-[420ms] ease-[cubic-bezier(0.22,1,0.36,1)] group-hover:-translate-y-full group-focus-visible:-translate-y-full">
                    {enlace.label}
                  </span>
                  <span
                    aria-hidden="true"
                    className="absolute inset-x-0 top-full block font-semibold text-[#1B2A22] transition-transform duration-[420ms] ease-[cubic-bezier(0.22,1,0.36,1)] group-hover:-translate-y-full group-focus-visible:-translate-y-full"
                  >
                    {enlace.label}
                  </span>
                </span>
                {esActiva && (
                  <motion.span
                    layoutId="punto-seccion-activa"
                    aria-hidden="true"
                    className="absolute bottom-[5px] left-1/2 -ml-[2.5px] h-[5px] w-[5px] rounded-full bg-sun-500 group-hover:bg-[#1B2A22]"
                    transition={{ type: 'spring', duration: 0.55, bounce: 0.25 }}
                  />
                )}
              </a>
            );
          })}
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
            <div className="relative" ref={menuCuenta}>
              <button
                ref={botonCuenta}
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
                <span className={showPanelNav ? 'hidden sm:inline lg:hidden xl:inline' : 'hidden sm:inline'}>
                  {user.roleLabel}
                </span>
              </button>

              <AnimatePresence>
              {userMenuOpen && (
                <motion.div
                  role="menu"
                  aria-label="Opciones de sesión"
                  initial={{ opacity: 0, y: -8, scale: 0.97 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: -8, scale: 0.97 }}
                  transition={{ duration: 0.16, ease: 'easeOut' }}
                  className="absolute right-0 mt-2 w-64 origin-top-right rounded-xl border border-line bg-surface p-3 text-ink shadow-[0_18px_40px_-12px_rgba(15,26,21,0.45)] z-50 space-y-2"
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
                </motion.div>
              )}
              </AnimatePresence>
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
            ref={botonMenu}
            type="button"
            onClick={() => setMobileMenuOpen((prev) => !prev)}
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-navigation-menu"
            aria-label={
              mobileMenuOpen ? 'Cerrar menú de navegación' : 'Abrir menú de navegación'
            }
            className="inline-flex min-h-11 min-w-11 items-center justify-center rounded-md border border-white/25 px-2.5 text-white hover:bg-forest-700 lg:hidden cursor-pointer"
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

      {/* Menú desplegable en celular y tableta */}
      <AnimatePresence>
      {mobileMenuOpen && (
        <motion.nav
          id="mobile-navigation-menu"
          aria-label="Navegación móvil"
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          exit={{ opacity: 0, height: 0 }}
          transition={{ duration: 0.22, ease: 'easeOut' }}
          className="absolute inset-x-0 top-full overflow-hidden border-y border-white/10 bg-forest-900 shadow-[0_24px_40px_-16px_rgba(0,0,0,0.6)] lg:hidden"
        >
          <ul className="flex flex-col gap-1 px-4 py-3 sm:px-6">
            {enlaces.map((enlace) => {
              const esActiva = activa === enlace.id;
              return (
                <li key={enlace.id}>
                  <a
                    href={hrefDe(enlace.id)}
                    onClick={(e) => alElegir(e, enlace.id)}
                    aria-current={esActiva ? 'true' : undefined}
                    className={`flex min-h-11 items-center justify-between rounded-full px-4 text-sm ${
                      esActiva ? 'bg-sun-500 font-semibold text-[#1B2A22]' : 'text-white/95 hover:bg-forest-700'
                    }`}
                  >
                    {enlace.label}
                    {esActiva && <span aria-hidden="true" className="h-1.5 w-1.5 rounded-full bg-[#1B2A22]" />}
                  </a>
                </li>
              );
            })}
            {showPanelNav ? null : user ? (
              <li>
                <Link
                  href="/panel"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex min-h-11 items-center rounded-full px-4 text-sm font-medium text-sun-500 hover:bg-forest-700"
                >
                  Ir al panel científico
                </Link>
              </li>
            ) : (
              <li>
                <Link
                  href="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex min-h-11 items-center rounded-full px-4 text-sm font-medium text-sun-500 hover:bg-forest-700"
                >
                  Iniciar sesión institucional
                </Link>
              </li>
            )}
          </ul>
        </motion.nav>
      )}
      </AnimatePresence>
    </header>
    </MotionConfig>
  );
}
