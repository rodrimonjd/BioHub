'use client';

import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Container } from '@/components/ui/Container';
import { Section } from '@/components/ui/Section';
import { SpeciesCard, SpeciesCardProps } from '@/components/biohub/SpeciesCard';
import {
  HistoryTimeline,
  HistoryEventItem,
} from '@/components/biohub/HistoryTimeline';
import { ProtectedLocation } from '@/components/biohub/ProtectedLocation';
import { ApiUsagePanel } from '@/components/biohub/ApiUsagePanel';
import {
  ReportStatus,
  BackgroundReportState,
} from '@/components/biohub/ReportStatus';
import { AuthModal } from '@/components/biohub/AuthModal';
import { FieldStationWorkspace } from '@/components/biohub/FieldStationWorkspace';
import { BioHubUser, DEMO_USERS } from '@/lib/permissions';
import { ConservationStatus } from '@/components/ui/ConservationBadge';

const INITIAL_SPECIES_CARDS: SpeciesCardProps[] = [
  {
    id: 'BH-0142',
    commonName: 'Rana dorada endémica',
    scientificName: 'Phyllobates terribilis',
    status: 'CR',
    specimenCode: 'EST-CHOCO-2026',
    illustrationVariant: 'amphibian',
    attributes: [
      { label: 'Temperatura del agua', value: '24.8 °C' },
      { label: 'Grabación acústica del canto', value: 'canto-endemico-44khz.wav' },
      { label: 'Humedad relativa de ribera', value: '92 %' },
      { label: 'Equipo del Estado asociado', value: 'Hidrófono #EST-H410 (Inv. García)' },
    ],
  },
  {
    id: 'BH-0289',
    commonName: 'Guacamaya roja amazónica',
    scientificName: 'Ara macao',
    status: 'LC',
    specimenCode: 'EST-AMAZ-2026',
    illustrationVariant: 'avian',
    attributes: [
      { label: 'Ruta de telemetría GPS', value: 'Tramo Río Caquetá → Chiribiquete' },
      { label: 'Muestra genética de ADN', value: 'ADN-AVE-2026-8812' },
      { label: 'Altitud media de vuelo', value: '380 msnm' },
      { label: 'Equipo del Estado asociado', value: 'Dron #EST-D921 (Inv. Rodríguez)' },
    ],
  },
  {
    id: 'BH-0315',
    commonName: 'Caimán del Orinoco',
    scientificName: 'Crocodylus intermedius',
    status: 'VU',
    specimenCode: 'EST-TOMO-2026',
    illustrationVariant: 'reptile',
    attributes: [
      { label: 'Temperatura de incubación nido', value: '31.4 °C' },
      { label: 'Muestra genética de ADN', value: 'ADN-ORI-2026-091' },
      { label: 'Longitud total (hocico-cola)', value: '385 cm' },
      { label: 'Equipo del Estado asociado', value: 'Sensor #EST-S104 (Inv. Escobar)' },
    ],
  },
];

const INITIAL_HISTORY_EVENTS: HistoryEventItem[] = [
  {
    id: 'EVT-2026-0114',
    version: 'v1',
    date: '2026-01-14 · 09:20 UTC-5',
    previousBelief: 'Sin registro previo (ingreso en estación base)',
    currentKnowledge: 'Crocodylus acutus (Clasificación morfológica preliminar)',
    authorizedBy: 'Inv. Sebastián García (inv-garcia-01)',
    reason:
      'Registro inicial al regresar de expedición de campo con mediciones de temperatura del agua y canto.',
  },
  {
    id: 'EVT-2026-0619',
    version: 'v2',
    date: '2026-06-19 · 16:45 UTC-5',
    previousBelief: 'Crocodylus acutus',
    currentKnowledge: 'Crocodylus intermedius',
    authorizedBy: 'Autor: inv-garcia-01 · Autorizó: alt-escobar-02 (Dra. Elena Escobar)',
    reason:
      'Secuenciación genética de muestra ADN-ORI-2026-884 confirma reclasificación definitiva tras análisis de laboratorio.',
  },
];

const CHALLENGES = [
  {
    code: 'RETO 01 · FICHAS Y ACTIVOS (HU-02.1 / HU-21.1)',
    title: 'Datos dispersos y esquemas cambiantes',
    description:
      'Reemplaza hojas de cálculo, correos y discos duros por fichas taxonómicas cuyos atributos varían según la especie y evolucionan cada año, manteniendo cada dron, cámara trampa, hidrófono y sensor asociado a un investigador responsable.',
  },
  {
    code: 'RETO 02 · TRÁFICO 60X (HU-05 / HU-14.1)',
    title: 'La consulta pública viral',
    description:
      'Cuando se anuncia el descubrimiento de una especie exótica, el tráfico se multiplica por más de sesenta en minutos. La ventana de consulta pública responde en menos de 1 segundo sin competir con la carga de datos científicos.',
  },
  {
    code: 'RETO 03 · TRAZABILIDAD (HU-09 / HU-10 / HU-19.1 / HU-20.1)',
    title: 'La ciencia se corrige sin borrar el pasado',
    description:
      'Un ejemplar reclasificado meses después mediante análisis genético conserva intacta su historia. Cualquier auditor científico puede comparar qué se creía en enero frente a diciembre, quién autorizó el cambio y por qué.',
  },
  {
    code: 'RETO 04 · CORREDOR BIOLÓGICO (HU-06 / HU-07 / HU-08.2)',
    title: 'Ubicaciones sensibles y plataforma abierta',
    description:
      'Oculta las coordenadas exactas de especies amenazadas al visitante común para frenar la caza ilegal, limita el consumo de universidades y ONGs a 60 req/min y ejecuta los reportes mensuales de deforestación en segundo plano.',
  },
];

export default function HomePage() {
  // Usuario autenticado (por defecto Investigador de Campo para poder operar HUs de inmediato, o cambiar a Visitante/Alto Rango)
  const [currentUser, setCurrentUser] = useState<BioHubUser>(DEMO_USERS[0]);
  const [authModalOpen, setAuthModalOpen] = useState<boolean>(false);

  // Estado para fichas taxonómicas (HU-02.1, HU-21.1, HU-05)
  const [speciesList, setSpeciesList] = useState<SpeciesCardProps[]>(
    INITIAL_SPECIES_CARDS
  );
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | ConservationStatus>('ALL');

  // Formulario para agregar atributo dinámico a una ficha existente (HU-21.1)
  const [selectedCardIdForAttr, setSelectedCardIdForAttr] = useState<string>(
    INITIAL_SPECIES_CARDS[0].id
  );
  const [newAttrLabel, setNewAttrLabel] = useState<string>('');
  const [newAttrValue, setNewAttrValue] = useState<string>('');

  // Historial científico (HU-09, HU-10, HU-20.1)
  const [activeFindingId, setActiveFindingId] = useState<string>('HAL-A1B2C3D4');
  const [historyEvents, setHistoryEvents] = useState<HistoryEventItem[]>(
    INITIAL_HISTORY_EVENTS
  );

  // Plataforma abierta y reportes (HU-16, HU-12.2)
  const [requestsPerMinute, setRequestsPerMinute] = useState<number>(36);
  const [reportState, setReportState] =
    useState<BackgroundReportState>('generando');
  const [darkMode, setDarkMode] = useState<boolean>(false);

  useEffect(() => {
    const root = document.documentElement;
    if (darkMode) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }, [darkMode]);

  const filteredSpecies = speciesList.filter((sp) => {
    const matchesText =
      sp.commonName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      sp.scientificName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      sp.specimenCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
      sp.attributes.some(
        (a) =>
          a.label.toLowerCase().includes(searchQuery.toLowerCase()) ||
          a.value.toLowerCase().includes(searchQuery.toLowerCase())
      );
    const matchesStatus = statusFilter === 'ALL' || sp.status === statusFilter;
    return matchesText && matchesStatus;
  });

  // HU-21.1: Agregar nuevo atributo taxonómico a una ficha sin rediseñar las demás
  const handleAddDynamicAttribute = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newAttrLabel.trim() || !newAttrValue.trim()) return;
    setSpeciesList((prev) =>
      prev.map((card) =>
        card.id === selectedCardIdForAttr
          ? {
              ...card,
              attributes: [
                ...card.attributes,
                { label: newAttrLabel.trim(), value: newAttrValue.trim() },
              ],
            }
          : card
      )
    );
    setNewAttrLabel('');
    setNewAttrValue('');
  };

  return (
    <div className="min-h-screen flex flex-col bg-paper text-ink">
      {/* Modal de Login y Cambio de Perfil Científico (HU-08.2 / HU-11.1) */}
      <AuthModal
        isOpen={authModalOpen}
        currentUser={currentUser}
        onClose={() => setAuthModalOpen(false)}
        onSelectUser={(user) => setCurrentUser(user)}
      />

      {/* 1. HEADER STICKY */}
      <header className="sticky top-0 z-40 border-b border-forest-700/40 bg-forest-900 text-white">
        <Container className="flex h-16 items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <a
              href="#inicio"
              className="font-serif text-2xl font-semibold tracking-tight text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 rounded-sm"
            >
              BioHub
            </a>
            <span className="hidden lg:inline-block font-mono text-xs text-white/75 border-l border-white/20 pl-3">
              Instituto Nacional de Biodiversidad
            </span>
          </div>

          <nav
            aria-label="Navegación principal"
            className="hidden md:flex items-center gap-5 text-sm text-white/85"
          >
            <a
              href="#estacion-campo"
              className="hover:text-white transition-colors duration-150 focus-visible:outline-none focus-visible:underline"
            >
              Estación de campo
            </a>
            <a
              href="#vitrina"
              className="hover:text-white transition-colors duration-150 focus-visible:outline-none focus-visible:underline"
            >
              Inventario y fichas
            </a>
            <a
              href="#trazabilidad"
              className="hover:text-white transition-colors duration-150 focus-visible:outline-none focus-visible:underline"
            >
              Trazabilidad y corredor
            </a>
            <a
              href="#operacion"
              className="hover:text-white transition-colors duration-150 focus-visible:outline-none focus-visible:underline"
            >
              API y reportes
            </a>
          </nav>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setDarkMode((prev) => !prev)}
              aria-pressed={darkMode}
              aria-label="Alternar modo oscuro naturalista"
              className="rounded-md border border-white/25 px-2.5 py-1.5 font-mono text-xs text-white/90 hover:bg-forest-700 transition-colors duration-150 cursor-pointer"
            >
              {darkMode ? 'Claro' : 'Oscuro'}
            </button>

            <button
              type="button"
              onClick={() => setAuthModalOpen(true)}
              className="inline-flex items-center gap-2 rounded-md bg-forest-700 px-3.5 py-1.5 text-xs font-medium text-white hover:bg-moss-500 hover:text-[#1B2A22] transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 cursor-pointer"
            >
              <span className="h-2 w-2 rounded-full bg-sun-500" aria-hidden="true" />
              <span>{currentUser.name}</span>
              <span className="hidden sm:inline font-mono opacity-80">
                ({currentUser.roleLabel})
              </span>
            </button>
          </div>
        </Container>
      </header>

      <main id="inicio" className="flex-1">
        {/* 2. HERO CON PATRÓN TOPOGRÁFICO (CURVAS DE NIVEL SVG INLINE, OPACIDAD 6%, SIN FOTOS) */}
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
              <div className="flex flex-wrap items-center gap-3">
                <p className="font-mono text-xs uppercase tracking-widest text-sun-500">
                  Archivo Científico Oficial · Inventario Nacional de Flora y Fauna
                </p>
                <span className="rounded bg-forest-700/80 px-2.5 py-0.5 font-mono text-xs text-white/90">
                  Sesión activa: {currentUser.roleLabel} ({currentUser.id})
                </span>
              </div>

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

              <div className="flex flex-wrap items-center gap-4 pt-2">
                <a href="#estacion-campo">
                  <Button
                    variant="primary"
                    className="border border-white/25 bg-forest-700 hover:bg-moss-500 hover:text-[#1B2A22]"
                  >
                    Ir a estación de registro (HU-01 / HU-13)
                  </Button>
                </a>
                <Button
                  variant="secondary"
                  onClick={() => setAuthModalOpen(true)}
                >
                  Cambiar usuario / Iniciar sesión
                </Button>
              </div>
            </div>
          </Container>
        </section>

        {/* 3. PROBLEMA: 4 TARJETAS CON LOS RETOS DEL INSTITUTO */}
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

        {/* 3.5 MÓDULO OPERATIVO DE ESTACIÓN DE CAMPO (HUs implementadas en servicio-registro:8081) */}
        <Section
          id="estacion-campo"
          aria-labelledby="field-station-heading"
          className="border-t border-line"
        >
          <div className="space-y-8">
            <div className="max-w-3xl space-y-2">
              <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                Estación base de registro · Integración con servicio-registro (:8081)
              </p>
              <h2
                id="field-station-heading"
                className="font-serif text-3xl font-semibold text-ink"
              >
                Operación científica de campo: avistamientos, hallazgos y reclasificaciones
              </h2>
              <p className="text-sm text-ink-muted">
                Desde aquí puedes ejecutar las historias de usuario ya implementadas en el
                backend (<code className="font-mono">POST /avistamientos</code>,{' '}
                <code className="font-mono">GET /avistamientos</code>,{' '}
                <code className="font-mono">POST /hallazgos</code>,{' '}
                <code className="font-mono">GET /hallazgos/&#123;id&#125;</code>,{' '}
                <code className="font-mono">PATCH /hallazgos/&#123;id&#125;</code> y{' '}
                <code className="font-mono">POST /hallazgos/&#123;id&#125;/reclasificacion</code>).
              </p>
            </div>

            <FieldStationWorkspace
              currentUser={currentUser}
              onOpenAuth={() => setAuthModalOpen(true)}
              onNewSpeciesCardCreated={(newCard) =>
                setSpeciesList((prev) => [newCard, ...prev])
              }
              onNewHistoryEventAdded={(newEvent, findingId) => {
                setActiveFindingId(`HAL-${findingId}`);
                setHistoryEvents((prev) => [...prev, newEvent]);
              }}
            />
          </div>
        </Section>

        {/* 4. VITRINA DE COMPONENTES DE DOMINIO */}
        <Section
          id="vitrina"
          aria-labelledby="showcase-heading"
          className="border-t border-line"
        >
          <div className="space-y-16">
            {/* 4.1 Fichas taxonómicas flexibles (SpeciesCard + Buscador HU-05 + Agregar atributo HU-21.1) */}
            <div className="space-y-6">
              <div className="flex flex-wrap items-end justify-between gap-4">
                <div className="max-w-2xl space-y-2">
                  <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                    1. Fichas taxonómicas flexibles · HU-02.1 / HU-05 / HU-21.1
                  </p>
                  <h2
                    id="showcase-heading"
                    className="font-serif text-3xl font-semibold text-ink"
                  >
                    Inventario público y plantillas por especie
                  </h2>
                  <p className="text-sm text-ink-muted">
                    Ningún registro se parece a otro: cada especie renderiza su propio conjunto
                    dinámico de atributos y el equipo físico del Estado asignado al investigador.
                  </p>
                </div>

                {/* Buscador y filtro por estado UICN (HU-05) */}
                <div className="flex flex-wrap items-center gap-2">
                  <input
                    type="search"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Buscar especie, ADN, equipo..."
                    aria-label="Buscar en el inventario de fichas"
                    className="rounded-md border border-line bg-surface px-3 py-2 text-xs text-ink focus:outline-none focus:ring-2 focus:ring-moss-500"
                  />
                  <select
                    value={statusFilter}
                    onChange={(e) =>
                      setStatusFilter(e.target.value as 'ALL' | ConservationStatus)
                    }
                    aria-label="Filtrar por categoría UICN"
                    className="rounded-md border border-line bg-surface px-3 py-2 font-mono text-xs text-ink"
                  >
                    <option value="ALL">Todas las categorías UICN</option>
                    <option value="CR">CR · En peligro crítico</option>
                    <option value="EN">EN · En peligro</option>
                    <option value="VU">VU · Vulnerable</option>
                    <option value="NT">NT · Casi amenazada</option>
                    <option value="LC">LC · Preocupación menor</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {filteredSpecies.map((species) => (
                  <SpeciesCard key={species.id} {...species} />
                ))}
              </div>

              {/* Constructor rápido para incorporar un nuevo atributo taxonómico a una ficha (HU-21.1) */}
              <Card className="bg-surface">
                <form
                  onSubmit={handleAddDynamicAttribute}
                  className="grid grid-cols-1 md:grid-cols-12 gap-3 items-end"
                >
                  <div className="md:col-span-3">
                    <label className="block font-mono text-[11px] uppercase tracking-wider text-moss-500 font-semibold mb-1">
                      HU-21.1 · Ficha objetivo
                    </label>
                    <select
                      value={selectedCardIdForAttr}
                      onChange={(e) => setSelectedCardIdForAttr(e.target.value)}
                      className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                    >
                      {speciesList.map((sp) => (
                        <option key={sp.id} value={sp.id}>
                          #{sp.id} — {sp.commonName}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="md:col-span-4">
                    <label className="block text-xs font-medium text-ink mb-1">
                      Nuevo atributo taxonómico (etiqueta)
                    </label>
                    <input
                      type="text"
                      required
                      value={newAttrLabel}
                      onChange={(e) => setNewAttrLabel(e.target.value)}
                      placeholder="Ej. pH del agua / Nivel de oxígeno"
                      className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                    />
                  </div>
                  <div className="md:col-span-3">
                    <label className="block text-xs font-medium text-ink mb-1">
                      Valor medido
                    </label>
                    <input
                      type="text"
                      required
                      value={newAttrValue}
                      onChange={(e) => setNewAttrValue(e.target.value)}
                      placeholder="Ej. 6.8 pH"
                      className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <Button type="submit" variant="ghost" className="w-full text-xs py-2">
                      + Agregar atributo
                    </Button>
                  </div>
                </form>
              </Card>
            </div>

            {/* 4.2 Historial científico y Ubicación protegida con toggle de permisos */}
            <div
              id="trazabilidad"
              className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start pt-4 border-t border-line"
            >
              <div className="lg:col-span-7 space-y-4">
                <div className="space-y-1">
                  <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                    2. La ciencia se corrige · HU-09 / HU-10 / HU-20.1
                  </p>
                  <p className="text-sm text-ink-muted">
                    Registro histórico inmutable de reclasificaciones y modificaciones (se actualiza en vivo al modificar o reclasificar arriba).
                  </p>
                </div>
                <HistoryTimeline
                  findingId={activeFindingId}
                  events={historyEvents}
                />
              </div>

              <div className="lg:col-span-5 space-y-4">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                      3. El corredor biológico · HU-06 / HU-07 / HU-08.2
                    </p>
                    <p className="text-sm text-ink-muted">
                      Vinculado a tu sesión activa ({currentUser.roleLabel}):
                    </p>
                  </div>

                  {/* Toggle accesible Público / Alto rango sincronizado con la sesión */}
                  <div
                    role="group"
                    aria-label="Seleccionar nivel de permiso para ubicación geográfica"
                    className="inline-flex rounded-md border border-line bg-surface p-1"
                  >
                    <button
                      type="button"
                      aria-pressed={currentUser.role === 'public'}
                      onClick={() => setCurrentUser(DEMO_USERS[4])} // Visitante común
                      className={`rounded px-2.5 py-1 font-mono text-xs transition-colors duration-150 cursor-pointer ${
                        currentUser.role === 'public'
                          ? 'bg-forest-900 text-white'
                          : 'text-ink-muted hover:text-ink'
                      }`}
                    >
                      Visitante común
                    </button>
                    <button
                      type="button"
                      aria-pressed={currentUser.role === 'senior'}
                      onClick={() => setCurrentUser(DEMO_USERS[1])} // Científico de Alto Rango
                      className={`rounded px-2.5 py-1 font-mono text-xs transition-colors duration-150 cursor-pointer ${
                        currentUser.role === 'senior'
                          ? 'bg-forest-900 text-white'
                          : 'text-ink-muted hover:text-ink'
                      }`}
                    >
                      Alto rango
                    </button>
                  </div>
                </div>

                <ProtectedLocation
                  role={currentUser.role}
                  speciesCommonName="Jaguar del corredor Macarena"
                  speciesScientificName="Panthera onca"
                  status="EN"
                  regionLabel="Región: Meta"
                  exactCoordinates="02° 42' 45.0&quot; N · 73° 54' 15.1&quot; W"
                  altitude="485 msnm"
                />
              </div>
            </div>

            {/* 4.3 Plataforma abierta (ApiUsagePanel) y Reportes en segundo plano (ReportStatus) */}
            <div
              id="operacion"
              className="grid grid-cols-1 lg:grid-cols-2 gap-8 pt-4 border-t border-line"
            >
              <div className="space-y-3">
                <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                  4. Plataforma abierta con límite de peticiones · HU-11.2 / HU-16
                </p>
                <ApiUsagePanel
                  consumerName="Universidad & ONG — Modelo Climático Regional"
                  endpointUrl="https://api.biohub.gov.co/v1/inventario"
                  requestsPerMinute={requestsPerMinute}
                  limitPerMinute={60}
                  onSimulateRequest={(delta) =>
                    setRequestsPerMinute((prev) => prev + delta)
                  }
                  onResetCounter={() => setRequestsPerMinute(18)}
                />
              </div>

              <div className="space-y-3">
                <p className="font-mono text-xs uppercase tracking-wider text-water-600 font-semibold">
                  5. Reportes analíticos sin bloqueo de campo · HU-12.2
                </p>
                <ReportStatus
                  reportTitle="Índice mensual de presión sobre ecosistemas y deforestación"
                  reportId="REP-2026-11"
                  recordsProcessed="2.450.000 registros"
                  state={reportState}
                  onChangeState={setReportState}
                />
              </div>
            </div>
          </div>
        </Section>
      </main>

      {/* 5. FOOTER SOBRIO EN FOREST-900 */}
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
