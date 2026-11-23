'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { Container } from '@/components/ui/Container';
import { Section } from '@/components/ui/Section';
import { SiteHeader } from '@/components/biohub/SiteHeader';
import { SpeciesCard, SpeciesCardProps } from '@/components/biohub/SpeciesCard';
import {
  HistoryTimeline,
  HistoryEventItem,
} from '@/components/biohub/HistoryTimeline';
import { ProtectedLocation } from '@/components/biohub/ProtectedLocation';
import { ApiUsagePanel } from '@/components/biohub/ApiUsagePanel';
import { ReportStatus } from '@/components/biohub/ReportStatus';
import { FieldStationWorkspace } from '@/components/biohub/FieldStationWorkspace';
import {
  actualizarAcceso,
  BioHubUser,
  canViewExactCoordinates,
  clearStoredSession,
  getStoredSession,
  PERMISOS,
  saveStoredSession,
  tienePermiso,
} from '@/lib/permissions';
import { obtenerAcceso } from '@/lib/autorizacion-api';
import { AdminUsuarios } from '@/components/biohub/AdminUsuarios';
import { BACKEND_SEED_SPECIES } from '@/lib/registro-api';
import {
  cargarInventario,
  EspecieRegistro,
  FichaTaxonomica,
  guardarAtributoEnFicha,
} from '@/lib/taxonomia-api';
import { ConservationStatus } from '@/components/ui/ConservationBadge';

const JAGUAR_SEED = BACKEND_SEED_SPECIES[1];

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
    status: 'CR',
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

const ILUSTRACION_POR_ESPECIE: Record<string, SpeciesCardProps['illustrationVariant']> = {
  'Crocodylus intermedius': 'reptile',
  'Ara macao': 'avian',
};

function codigoFicha(especieId: string): string {
  return especieId.slice(0, 8).toUpperCase();
}

function tarjetaDesde(especie: EspecieRegistro, ficha: FichaTaxonomica | undefined): SpeciesCardProps {
  const atributos = (ficha?.atributos ?? [])
    .filter((a) => a.atributo && a.valor)
    .map((a) => ({
      label: a.atributo!.unidad ? `${a.atributo!.nombre} (${a.atributo!.unidad})` : a.atributo!.nombre,
      value: a.valor as string,
    }));
  return {
    id: codigoFicha(especie.id),
    commonName: especie.nombreComun,
    scientificName: especie.nombreCientifico,
    status: especie.estadoConservacion as ConservationStatus,
    specimenCode: ficha ? `Ficha v${ficha.version}` : 'Sin ficha taxonómica',
    illustrationVariant: ILUSTRACION_POR_ESPECIE[especie.nombreCientifico],
    attributes: atributos,
  };
}

const INITIAL_HISTORY_EVENTS: HistoryEventItem[] = [
  {
    id: 'EVT-2026-0114',
    version: 'v1',
    date: '2026-01-14 · 09:20 UTC-5',
    previousBelief: 'Sin registro previo (ingreso en estación base)',
    currentKnowledge: 'Crocodylus acutus (Clasificación morfológica preliminar)',
    authorizedBy: 'Sebastián García (Investigador de campo)',
    reason:
      'Registro inicial al regresar de expedición de campo con mediciones de temperatura del agua y canto.',
  },
  {
    id: 'EVT-2026-0619',
    version: 'v2',
    date: '2026-06-19 · 16:45 UTC-5',
    previousBelief: 'Crocodylus acutus',
    currentKnowledge: 'Crocodylus intermedius',
    authorizedBy: 'Sebastián García · Autorizó: Dra. Elena Escobar',
    reason:
      'Secuenciación genética de muestra ADN-ORI-2026-884 confirma reclasificación definitiva tras análisis de laboratorio.',
  },
];

export default function PanelPage() {
  const router = useRouter();
  const [currentUser, setCurrentUser] = useState<BioHubUser | null>(null);
  const [checkingSession, setCheckingSession] = useState<boolean>(true);

<<<<<<< HEAD
  // Estado de fichas taxonómicas
=======
  const [locationPreviewRole, setLocationPreviewRole] = useState<UserRole>('public');

>>>>>>> origin/desarrollo
  const [speciesList, setSpeciesList] = useState<SpeciesCardProps[]>(
    INITIAL_SPECIES_CARDS
  );
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | ConservationStatus>('ALL');

  const [selectedCardIdForAttr, setSelectedCardIdForAttr] = useState<string>(
    INITIAL_SPECIES_CARDS[0].id
  );
  const [newAttrLabel, setNewAttrLabel] = useState<string>('');
  const [newAttrValue, setNewAttrValue] = useState<string>('');
  const [attrSavedNotice, setAttrSavedNotice] = useState<string | null>(null);
  const [attrError, setAttrError] = useState<string | null>(null);
  const [guardandoAtributo, setGuardandoAtributo] = useState<boolean>(false);

  const [inventario, setInventario] = useState<{
    especies: EspecieRegistro[];
    fichas: FichaTaxonomica[];
  } | null>(null);

  const [activeFindingId, setActiveFindingId] = useState<string>('HAL-A1B2C3D4');
  const [historyEvents, setHistoryEvents] = useState<HistoryEventItem[]>(
    INITIAL_HISTORY_EVENTS
  );

  useEffect(() => {
    const stored = getStoredSession();
    if (!stored) {
      setCheckingSession(false);
      router.replace('/login');
      return;
    }
    setCurrentUser(stored);
    setCheckingSession(false);

    // Los permisos pueden haber cambiado desde el inicio de sesión: se piden de nuevo a autorización.
    obtenerAcceso(stored.id).then((acceso) => {
      if (!acceso) return; // servicio caído: se mantiene la sesión guardada
      const actualizado = actualizarAcceso(stored, acceso);
      saveStoredSession(actualizado);
      setCurrentUser(actualizado);
    });
  }, [router]);

<<<<<<< HEAD
  // Al cerrar sesión se vuelve a la página principal
=======
  const recargarInventario = async () => {
    const datos = await cargarInventario();
    if (!datos) return;
    setInventario(datos);
    const tarjetas = datos.especies.map((especie) =>
      tarjetaDesde(especie, datos.fichas.find((f) => f.especieId === especie.id))
    );
    setSpeciesList(tarjetas);
    setSelectedCardIdForAttr((actual) =>
      tarjetas.some((t) => t.id === actual) ? actual : tarjetas[0]?.id ?? actual
    );
  };

  useEffect(() => {
    recargarInventario();
  }, []);

>>>>>>> origin/desarrollo
  const handleLogout = () => {
    clearStoredSession();
    setCurrentUser(null);
    router.push('/');
  };

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

  const handleAddDynamicAttribute = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newAttrLabel.trim() || !newAttrValue.trim()) return;
    setAttrError(null);
    setAttrSavedNotice(null);

    const especie = inventario?.especies.find((sp) => codigoFicha(sp.id) === selectedCardIdForAttr);
    if (inventario && especie && currentUser) {
      // Conectado: se guarda en servicio-taxonomia
      setGuardandoAtributo(true);
      try {
        const ficha = await guardarAtributoEnFicha({
          especieId: especie.id,
          ficha: inventario.fichas.find((f) => f.especieId === especie.id) ?? null,
          nombre: newAttrLabel.trim(),
          valor: newAttrValue.trim(),
          autor: currentUser.id,
        });
        setAttrSavedNotice(
          `Atributo "${newAttrLabel.trim()}" guardado en la ficha de ${especie.nombreComun} (versión ${ficha.version}).`
        );
        setNewAttrLabel('');
        setNewAttrValue('');
        await recargarInventario();
      } catch (error) {
        setAttrError(error instanceof Error ? error.message : 'No se pudo guardar el atributo.');
      } finally {
        setGuardandoAtributo(false);
      }
      return;
    }

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
    setAttrSavedNotice(
      `Atributo "${newAttrLabel.trim()}" incorporado a la ficha #${selectedCardIdForAttr}.`
    );
    setNewAttrLabel('');
    setNewAttrValue('');
  };

  if (checkingSession) {
    return (
      <div className="min-h-screen flex flex-col bg-paper text-ink">
        <SiteHeader user={null} showPanelNav={false} />
        <main className="flex-1 flex items-center justify-center p-8">
          <p role="status" aria-live="polite" className="font-mono text-sm text-ink-muted">
            Verificando sesión institucional...
          </p>
        </main>
      </div>
    );
  }

  if (!currentUser) {
    return (
      <div className="min-h-screen flex flex-col bg-paper text-ink">
        <SiteHeader user={null} showPanelNav={false} />
        <main className="flex-1">
          <Section>
            <Card className="mx-auto max-w-lg text-center space-y-4">
              <h1 className="font-serif text-2xl font-semibold text-ink">
                Sesión requerida
              </h1>
              <p className="text-sm text-ink-muted">
                Para consultar el panel científico y registrar hallazgos debes iniciar
                sesión con tu perfil institucional.
              </p>
              <div className="pt-2">
                <Link
                  href="/login"
                  className="inline-flex min-h-11 items-center justify-center rounded-md bg-forest-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-forest-700"
                >
                  Ir a iniciar sesión
                </Link>
              </div>
            </Card>
          </Section>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col bg-paper text-ink">
      <SiteHeader
        user={currentUser}
        onLogout={handleLogout}
        showPanelNav={true}
      />

      <main className="flex-1">
        {/* 1. ESTACIÓN DE CAMPO */}
        <Section id="estacion-campo" aria-labelledby="field-station-heading">
          <div className="space-y-8">
            <div className="max-w-3xl space-y-2">
              <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                Estación base de registro · {currentUser.station}
              </p>
              <h1
                id="field-station-heading"
                className="font-serif text-3xl font-semibold text-ink"
              >
                Operación científica de campo
              </h1>
              <p className="text-sm text-ink-muted">
                Registra avistamientos diarios, documenta nuevos hallazgos biológicos con
                sus evidencias de campo y mantén actualizadas las reclasificaciones
                taxonómicas conservando cada versión anterior.
              </p>
            </div>

            <FieldStationWorkspace
              currentUser={currentUser}
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

        {/* ADMINISTRACIÓN DE USUARIOS: solo con el permiso roles:administrar */}
        {tienePermiso(currentUser, PERMISOS.ADMINISTRAR_ROLES) && (
          <Section
            id="administracion"
            aria-labelledby="admin-heading"
            className="border-t border-line"
          >
            <div className="space-y-8">
              <div className="max-w-3xl space-y-2">
                <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                  Administración · Acceso controlado
                </p>
                <h2 id="admin-heading" className="font-serif text-3xl font-semibold text-ink">
                  Usuarios y roles
                </h2>
                <p className="text-sm text-ink-muted">
                  Registra las cuentas institucionales del personal y asigna su rol. Los permisos de
                  cada rol los define el servicio de autorización.
                </p>
              </div>
              <AdminUsuarios admin={currentUser} />
            </div>
          </Section>
        )}

        {/* 2. INVENTARIO Y FICHAS TAXONÓMICAS */}
        <Section
          id="inventario"
          aria-labelledby="showcase-heading"
          className="border-t border-line"
        >
          <div className="space-y-12">
            <div className="space-y-6">
              <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
                <div className="max-w-2xl space-y-2">
                  <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                    Catálogo taxonómico flexible
                  </p>
                  <h2
                    id="showcase-heading"
                    className="font-serif text-3xl font-semibold text-ink"
                  >
                    Inventario y fichas por especie
                  </h2>
                  <p className="text-sm text-ink-muted">
                    Explora las fichas registradas en estación base y despliega los
                    atributos específicos medidos para cada ejemplar.
                  </p>
                  <p className="font-mono text-xs text-ink-muted">
                    {inventario
                      ? 'Conectado a servicio-taxonomía · especies de servicio-registro'
                      : 'Fichas de demostración (servicio-taxonomía sin conexión)'}
                  </p>
                </div>

                {/* Buscador amplio y filtro por estado UICN */}
                <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full lg:w-auto">
                  <input
                    id="search-species-input"
                    type="search"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Buscar especie, código o atributo..."
                    aria-label="Buscar en el inventario de fichas"
                    className="min-h-11 w-full sm:w-80 rounded-md border border-line bg-surface px-3.5 py-2 text-xs text-ink focus:outline-none focus:ring-2 focus:ring-moss-500"
                  />
                  <select
                    id="filter-iucn-select"
                    value={statusFilter}
                    onChange={(e) =>
                      setStatusFilter(e.target.value as 'ALL' | ConservationStatus)
                    }
                    aria-label="Filtrar por categoría UICN"
                    className="min-h-11 rounded-md border border-line bg-surface px-3.5 py-2 font-mono text-xs text-ink"
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

              {/* Estado vacío cuando ninguna ficha coincide con la búsqueda */}
              {filteredSpecies.length === 0 ? (
                <Card className="py-12 text-center space-y-3">
                  <p className="font-serif text-lg font-semibold text-ink">
                    Ninguna ficha coincide con &ldquo;{searchQuery || statusFilter}&rdquo;
                  </p>
                  <p className="text-xs text-ink-muted">
                    Intenta con otro término de búsqueda o restablece los filtros activos.
                  </p>
                  <div className="pt-1">
                    <Button
                      variant="ghost"
                      onClick={() => {
                        setSearchQuery('');
                        setStatusFilter('ALL');
                      }}
                      className="text-xs"
                    >
                      Limpiar filtros
                    </Button>
                  </div>
                </Card>
              ) : (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  {filteredSpecies.map((species) => (
                    <SpeciesCard key={species.id} {...species} />
                  ))}
                </div>
              )}

              {/* Formulario con título y explicación clara: Agregar atributo a una ficha */}
              <Card className="bg-surface space-y-4">
                <div className="border-b border-line pb-3">
                  <h3 className="font-serif text-lg font-semibold text-ink">
                    Agregar atributo a una ficha
                  </h3>
                  <p className="text-xs text-ink-muted">
                    Incorpora un nuevo parámetro de medición a cualquiera de las fichas
                    existentes sin alterar la estructura de las demás especies.
                  </p>
                </div>

                <div aria-live="polite" className="space-y-2">
                  {attrSavedNotice && (
                    <p className="rounded-md border border-iucn-lc bg-iucn-lc/10 px-3 py-2 font-mono text-xs text-ink">
                      {attrSavedNotice}
                    </p>
                  )}
                  {attrError && (
                    <p role="alert" className="rounded-md border border-iucn-cr bg-iucn-cr/10 px-3 py-2 font-mono text-xs text-ink">
                      {attrError}
                    </p>
                  )}
                </div>

                <form
                  onSubmit={handleAddDynamicAttribute}
                  className="grid grid-cols-1 md:grid-cols-12 gap-3 items-end"
                >
                  <Field
                    id="attr-ficha-select"
                    label="Ficha taxonómica"
                    className="md:col-span-3"
                  >
                    <select
                      id="attr-ficha-select"
                      value={selectedCardIdForAttr}
                      onChange={(e) => setSelectedCardIdForAttr(e.target.value)}
                      className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                    >
                      {speciesList.map((sp) => (
                        <option key={sp.id} value={sp.id}>
                          #{sp.id} — {sp.commonName}
                        </option>
                      ))}
                    </select>
                  </Field>

                  <Field
                    id="attr-label-input"
                    label="Nombre del atributo"
                    className="md:col-span-4"
                  >
                    <input
                      id="attr-label-input"
                      type="text"
                      required
                      value={newAttrLabel}
                      onChange={(e) => setNewAttrLabel(e.target.value)}
                      placeholder="Ej. pH del agua / Nivel de oxígeno"
                      className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                    />
                  </Field>

                  <Field
                    id="attr-value-input"
                    label="Valor registrado"
                    className="md:col-span-3"
                  >
                    <input
                      id="attr-value-input"
                      type="text"
                      required
                      value={newAttrValue}
                      onChange={(e) => setNewAttrValue(e.target.value)}
                      placeholder="Ej. 6.8 pH"
                      className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                    />
                  </Field>

                  <div className="md:col-span-2">
                    <Button
                      type="submit"
                      variant="ghost"
                      className="w-full text-xs"
                      disabled={guardandoAtributo}
                    >
                      {guardandoAtributo ? 'Guardando...' : '+ Agregar atributo'}
                    </Button>
                  </div>
                </form>
              </Card>
            </div>

            {/* 3. TRAZABILIDAD Y CORREDOR BIOLÓGICO */}
            <div
              id="trazabilidad"
              className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start pt-8 border-t border-line"
            >
              <div className="lg:col-span-7 space-y-4">
                <div className="space-y-1">
                  <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                    La ciencia se corrige
                  </p>
                  <p className="text-sm text-ink-muted">
                    Registro histórico inmutable de reclasificaciones y modificaciones
                    autorizadas.
                  </p>
                </div>
                <HistoryTimeline
                  findingId={activeFindingId}
                  events={historyEvents}
                />
              </div>

              <div className="lg:col-span-5 space-y-4">
                <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                  El corredor biológico
                </p>

                {/* La visibilidad depende del permiso ubicacion:ver_exacta de la cuenta (servicio-autorizacion) */}
                <ProtectedLocation
                  puedeVerExacta={canViewExactCoordinates(currentUser)}
                  nivelLabel={currentUser.roleLabel}
                  speciesCommonName={`${JAGUAR_SEED.commonName} del corredor Macarena`}
                  speciesScientificName={JAGUAR_SEED.scientificName}
                  status={JAGUAR_SEED.status}
                  regionLabel="Región: Meta"
                  exactCoordinates="02° 42' 45.0&quot; N · 73° 54' 15.1&quot; W"
                  altitude="485 msnm"
                />
              </div>
            </div>

            {/* 4. PLATAFORMA ABIERTA Y REPORTES EN SEGUNDO PLANO (sin botones de simulación falsos) */}
            <div
              id="operacion"
              className="grid grid-cols-1 lg:grid-cols-2 gap-8 pt-8 border-t border-line"
            >
              <div className="space-y-3">
                <p className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                  Plataforma abierta con límite de peticiones
                </p>
                <ApiUsagePanel
                  consumerName="Universidad & ONG — Modelo Climático Regional"
                  endpointUrl="https://api.biohub.gov.co/v1/inventario"
                  requestsPerMinute={36}
                  limitPerMinute={60}
                />
              </div>

              <div className="space-y-3">
                <p className="font-mono text-xs uppercase tracking-wider text-water-600 font-semibold">
                  Reportes analíticos sin bloqueo de campo
                </p>
                <ReportStatus
                  reportTitle="Índice mensual de presión sobre ecosistemas y deforestación"
                  reportId="REP-2026-11"
                  recordsProcessed="2.450.000 registros"
                  state="generando"
                />
              </div>
            </div>
          </div>
        </Section>
      </main>

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
