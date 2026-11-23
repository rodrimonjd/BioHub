'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Card } from '@/components/ui/Card';
import { Container } from '@/components/ui/Container';
import { Section } from '@/components/ui/Section';
import { SiteHeader } from '@/components/biohub/SiteHeader';
import {
  BioHubUser,
  clearStoredSession,
  crearSesionInvitado,
  getStoredSession,
  ID_INVITADO,
  saveStoredSession,
} from '@/lib/permissions';
import { obtenerAcceso } from '@/lib/autorizacion-api';

const CHALLENGES = [
  {
    code: 'RETO 01 · FICHAS Y ACTIVOS',
    title: 'Datos dispersos y esquemas cambiantes',
    description:
      'Reemplaza hojas de cálculo, correos y discos duros por fichas taxonómicas cuyos atributos varían según la especie y evolucionan cada año, manteniendo cada dron, cámara trampa, hidrófono y sensor asociado a un investigador responsable.',
  },
  {
    code: 'RETO 02 · TRÁFICO 60X',
    title: 'La consulta pública viral',
    description:
      'Cuando se anuncia el descubrimiento de una especie exótica, el tráfico se multiplica por más de sesenta en minutos. La ventana de consulta pública responde al instante sin competir por recursos con la carga de datos científicos.',
  },
  {
    code: 'RETO 03 · TRAZABILIDAD',
    title: 'La ciencia se corrige sin borrar el pasado',
    description:
      'Un ejemplar reclasificado meses después mediante análisis genético conserva intacta su historia. Cualquier auditor científico puede comparar qué se creía en enero frente a diciembre, quién autorizó el cambio y por qué.',
  },
  {
    code: 'RETO 04 · CORREDOR BIOLÓGICO',
    title: 'Ubicaciones sensibles y plataforma abierta',
    description:
      'Oculta las coordenadas exactas de especies amenazadas al visitante común para frenar la caza ilegal, limita el consumo de universidades y ONGs por minuto y ejecuta los reportes mensuales de deforestación en segundo plano.',
  },
];

export default function HomePage() {
  const router = useRouter();
  const [sessionUser, setSessionUser] = useState<BioHubUser | null>(null);
  const [entrandoInvitado, setEntrandoInvitado] = useState<boolean>(false);

  useEffect(() => {
    setSessionUser(getStoredSession());
  }, []);

  const handleLogout = () => {
    clearStoredSession();
    setSessionUser(null);
  };

  // Entrar sin cuenta: los permisos los da servicio-autorizacion (un usuario sin roles es CIUDADANO, solo consulta).
  const handleInvitado = async () => {
    setEntrandoInvitado(true);
    try {
      const sesion = crearSesionInvitado(await obtenerAcceso(ID_INVITADO));
      saveStoredSession(sesion);
      router.push('/panel');
    } finally {
      setEntrandoInvitado(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-paper text-ink">
      <SiteHeader
        user={sessionUser}
        onLogout={handleLogout}
        showPanelNav={false}
      />

      <main id="inicio" className="flex-1">
        {/* HERO CON PATRÓN TOPOGRÁFICO (CURVAS DE NIVEL SVG INLINE, OPACIDAD 6%, SIN FOTOS) */}
        <section
          aria-labelledby="hero-heading"
          className="relative overflow-hidden bg-forest-900 py-24 text-white"
        >
          <svg
            className="pointer-events-none absolute inset-0 h-full w-full opacity-[0.06]"
            viewBox="0 0 1200 600"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            aria-hidden="true"
            preserveAspectRatio="xMidYMid slice"
          >
            <g stroke="#FFFFFF" strokeWidth="1.5">
              <path d="M-100 90 C220 20, 460 180, 780 90 C1000 30, 1150 130, 1320 80" />
              <path d="M-100 155 C200 85, 480 240, 800 150 C1020 90, 1140 190, 1320 140" />
              <path d="M-100 225 C190 155, 510 310, 820 215 C1040 150, 1160 250, 1320 205" />
              <path d="M-100 295 C210 225, 530 375, 840 285 C1050 220, 1170 315, 1320 275" />
              <path d="M-100 365 C240 295, 550 445, 860 355 C1060 290, 1180 385, 1320 345" />
              <path d="M-100 435 C260 365, 570 510, 880 425 C1080 360, 1190 455, 1320 415" />
              <path d="M-100 505 C280 435, 590 575, 900 495 C1100 430, 1200 520, 1320 485" />
              <ellipse cx="880" cy="240" rx="220" ry="85" />
              <ellipse cx="880" cy="240" rx="140" ry="52" />
              <ellipse cx="880" cy="240" rx="70" ry="24" />
              <ellipse cx="290" cy="390" rx="190" ry="70" />
              <ellipse cx="290" cy="390" rx="110" ry="38" />
            </g>
          </svg>

          <Container className="relative z-10">
            <div className="max-w-3xl space-y-6">
              <p className="font-mono text-xs uppercase tracking-widest text-sun-500">
                Archivo Científico Oficial · Inventario Nacional de Flora y Fauna
              </p>

              <h1
                id="hero-heading"
                className="font-serif text-4xl sm:text-5xl font-semibold leading-tight tracking-tight text-white"
              >
                Sistema Nacional de Registro y Vigilancia de Biodiversidad
              </h1>
              <p className="text-base sm:text-lg text-white/85 leading-relaxed">
                BioHub centraliza los hallazgos de las expediciones biológicas del país,
                resguarda las coordenadas de especies en peligro frente a la caza ilegal,
                preserva cada reclasificación taxonómica y divulga el patrimonio natural
                sin comprometer la operación científica de campo.
              </p>

              {/* Jerarquía de botones corregida y HTML válido (sin <button> dentro de <a>) */}
              <div className="flex flex-wrap items-center gap-4 pt-2">
                <Link
                  href={sessionUser ? '/panel' : '/login'}
                  className="inline-flex min-h-11 items-center justify-center rounded-md bg-sun-500 px-5 py-2.5 text-sm font-semibold text-[#1B2A22] hover:brightness-95 transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white"
                >
                  {sessionUser
                    ? 'Entrar al panel científico'
                    : 'Iniciar sesión institucional'}
                </Link>
                {!sessionUser && (
                  <button
                    type="button"
                    onClick={handleInvitado}
                    disabled={entrandoInvitado}
                    title="Entrar sin cuenta, con permisos de visitante común (solo consulta)"
                    className="inline-flex min-h-11 items-center justify-center rounded-md border border-white/30 bg-forest-700/60 px-5 py-2.5 text-sm font-medium text-white hover:bg-forest-700 transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 disabled:opacity-60 cursor-pointer"
                  >
                    {entrandoInvitado ? 'Entrando…' : 'Login como invitado'}
                  </button>
                )}
                <a
                  href="#desafios"
                  className="inline-flex min-h-11 items-center justify-center rounded-md border border-white/30 bg-forest-700/60 px-5 py-2.5 text-sm font-medium text-white hover:bg-forest-700 transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500"
                >
                  Conocer los desafíos científicos
                </a>
              </div>
            </div>
          </Container>
        </section>

        {/* PROBLEMA: 4 TARJETAS CON LOS RETOS DEL INSTITUTO */}
        <Section id="desafios" aria-labelledby="challenges-heading">
          <div className="space-y-10">
            <div className="max-w-2xl space-y-2">
              <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                Fundamento arquitectónico
              </p>
              <h2
                id="challenges-heading"
                className="font-serif text-3xl font-semibold text-ink"
              >
                Por qué el inventario nacional exige una arquitectura especializada
              </h2>
              <p className="text-sm sm:text-base text-ink-muted">
                Durante años la información biológica estuvo fragmentada en hojas de cálculo,
                correos y discos duros. BioHub resuelve cuatro desafíos estructurales.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {CHALLENGES.map((challenge) => (
                <Card key={challenge.code} as="article" className="space-y-2.5">
                  <p className="font-mono text-xs font-semibold text-moss-500">
                    {challenge.code}
                  </p>
                  <h3 className="font-serif text-xl font-semibold text-ink">
                    {challenge.title}
                  </h3>
                  <p className="text-sm text-ink-muted leading-relaxed">
                    {challenge.description}
                  </p>
                </Card>
              ))}
            </div>
          </div>
        </Section>
      </main>

      {/* FOOTER SOBRIO EN FOREST-900 */}
      <footer className="bg-forest-900 text-white border-t border-forest-700/40 py-12">
        <Container className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
          <div className="space-y-1">
            <p className="font-serif text-xl font-semibold text-white">BioHub</p>
            <p className="text-xs text-white/75">
              Sistema Nacional de Registro y Vigilancia de Biodiversidad · Instituto Nacional de Biodiversidad
            </p>
          </div>
          <div className="font-mono text-xs text-white/70 space-y-1 sm:text-right">
            <p>Ingeniería de Software 2 · Universidad El Bosque</p>
            <p>Estándar taxonómico y categorías UICN (LC · NT · VU · EN · CR)</p>
          </div>
        </Container>
      </footer>
    </div>
  );
}
