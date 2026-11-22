'use client';

import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { ConservationBadge } from '@/components/ui/ConservationBadge';
import {
  BioHubUser,
  canRegisterFieldData,
  canAuthorizeReclassification,
} from '@/lib/permissions';
import {
  AvistamientoResponse,
  BACKEND_SEED_SPECIES,
  EvidenciaRequest,
  HallazgoResponse,
  listarAvistamientos,
  modificarHallazgo,
  obtenerHallazgoPorId,
  reclasificarHallazgo,
  registrarAvistamiento,
  registrarHallazgo,
  TipoEvidencia,
} from '@/lib/registro-api';
import { SpeciesCardProps } from './SpeciesCard';
import { HistoryEventItem } from './HistoryTimeline';

export interface FieldStationWorkspaceProps {
  currentUser: BioHubUser;
  onOpenAuth: () => void;
  onNewSpeciesCardCreated: (card: SpeciesCardProps) => void;
  onNewHistoryEventAdded: (event: HistoryEventItem, findingId: string) => void;
}

const DEPARTAMENTOS_REGIONES: { depto: string; region: string }[] = [
  { depto: 'Vichada', region: 'Orinoquía' },
  { depto: 'Meta', region: 'Orinoquía' },
  { depto: 'Casanare', region: 'Orinoquía' },
  { depto: 'Amazonas', region: 'Amazonía' },
  { depto: 'Caquetá', region: 'Amazonía' },
  { depto: 'Chocó', region: 'Pacífico' },
  { depto: 'Antioquia', region: 'Andina' },
];

const INITIAL_LOCAL_AVISTAMIENTOS: AvistamientoResponse[] = [
  {
    id: 'a891d014-5011-4092-9210-102938475610',
    especieId: '11111111-1111-1111-1111-111111111111',
    especie: 'Crocodylus intermedius',
    fechaHora: '2026-11-21T13:15:00Z',
    cantidad: 2,
    latitud: 5.5412,
    longitud: -68.5194,
    departamento: 'Vichada',
    region: 'Orinoquía',
    creadoEn: '2026-11-21T13:15:01Z',
  },
  {
    id: 'b772e190-2218-4881-8120-991827364500',
    especieId: '22222222-2222-2222-2222-222222222222',
    especie: 'Panthera onca',
    fechaHora: '2026-11-21T08:40:00Z',
    cantidad: 1,
    latitud: 2.7125,
    longitud: -73.9042,
    departamento: 'Meta',
    region: 'Orinoquía',
    creadoEn: '2026-11-21T08:40:01Z',
  },
];

const INITIAL_LOCAL_HALLAZGOS: HallazgoResponse[] = [
  {
    id: 'a1b2c3d4-1111-4000-8000-000000000001',
    investigadorId: 'inv-garcia-01',
    especieId: '11111111-1111-1111-1111-111111111111',
    especie: 'Crocodylus intermedius',
    fichaId: 'FICHA-ORI-2026',
    fecha: '2026-11-18T14:20:00Z',
    descripcion:
      'Hembra adulta nidificando en banco de arena aislado del río Tomo. Se toma telemetría térmica del agua y biopsia genética.',
    latitud: 5.5428,
    longitud: -68.5214,
    departamento: 'Vichada',
    region: 'Orinoquía',
    versionActual: 2,
    creadoEn: '2026-11-18T14:25:00Z',
    evidencias: [
      {
        id: 'ev-101',
        tipo: 'TEMPERATURA',
        valor: '31.4',
        unidad: 'C',
        urlArchivo: null,
        atributoTaxonomico: 'Temperatura de incubación nido',
      },
      {
        id: 'ev-102',
        tipo: 'ADN',
        valor: 'ADN-ORI-2026-884',
        unidad: null,
        urlArchivo: null,
        atributoTaxonomico: 'Muestra genética de ADN',
      },
    ],
  },
];

export function FieldStationWorkspace({
  currentUser,
  onOpenAuth,
  onNewSpeciesCardCreated,
  onNewHistoryEventAdded,
}: FieldStationWorkspaceProps) {
  const [activeTab, setActiveTab] = useState<
    'avistamientos' | 'hallazgos' | 'reclasificacion'
  >('avistamientos');
  const [backendOnline, setBackendOnline] = useState<boolean>(false);
  const [statusBanner, setStatusBanner] = useState<{
    type: 'ok' | 'err';
    text: string;
  } | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  // Listas de datos (sincronizadas con :8081 o local)
  const [avistamientos, setAvistamientos] = useState<AvistamientoResponse[]>(
    INITIAL_LOCAL_AVISTAMIENTOS
  );
  const [hallazgos, setHallazgos] = useState<HallazgoResponse[]>(
    INITIAL_LOCAL_HALLAZGOS
  );

  // Estado formulario HU-13: POST /avistamientos
  const [avEspecieId, setAvEspecieId] = useState(BACKEND_SEED_SPECIES[0].id);
  const [avCantidad, setAvCantidad] = useState<number>(2);
  const [avDepto, setAvDepto] = useState('Vichada');
  const [avRegion, setAvRegion] = useState('Orinoquía');
  const [avLat, setAvLat] = useState('5.5412');
  const [avLon, setAvLon] = useState('-68.5194');

  // Estado formulario HU-01 + HU-03: POST /hallazgos
  const [haEspecieId, setHaEspecieId] = useState(BACKEND_SEED_SPECIES[0].id);
  const [haFichaId, setHaFichaId] = useState('FICHA-TOMO-2026');
  const [haDescripcion, setHaDescripcion] = useState('');
  const [haDepto, setHaDepto] = useState('Vichada');
  const [haRegion, setHaRegion] = useState('Orinoquía');
  const [haLat, setHaLat] = useState('5.5428');
  const [haLon, setHaLon] = useState('-68.5214');
  const [haEvidencias, setHaEvidencias] = useState<EvidenciaRequest[]>([
    {
      tipo: 'TEMPERATURA',
      valor: '28.5',
      unidad: 'C',
      atributoTaxonomico: 'Temperatura del agua',
    },
    {
      tipo: 'ADN',
      valor: 'ADN-2026-901',
      atributoTaxonomico: 'Muestra genética de ADN',
    },
  ]);

  // Estado HU-19.1 & HU-10: Modificación (PATCH /hallazgos/{id}) y Reclasificación (POST /hallazgos/{id}/reclasificacion)
  const [targetHallazgoId, setTargetHallazgoId] = useState<string>(
    INITIAL_LOCAL_HALLAZGOS[0].id
  );
  const [lookupUuid, setLookupUuid] = useState<string>('');
  const [modCampo, setModCampo] = useState<'descripcion' | 'departamento' | 'region'>(
    'descripcion'
  );
  const [modValor, setModValor] = useState<string>('');
  const [recNuevaEspecieId, setRecNuevaEspecieId] = useState<string>(
    BACKEND_SEED_SPECIES[1].id
  );
  const [recMotivo, setRecMotivo] = useState<string>('');
  const [recAutorizador, setRecAutorizador] = useState<string>('alt-escobar-02');

  const canWrite = canRegisterFieldData(currentUser.role);

  const syncWithBackend = async () => {
    const res = await listarAvistamientos();
    setBackendOnline(res.online);
    if (res.online && res.data) {
      setAvistamientos(res.data.length > 0 ? res.data : INITIAL_LOCAL_AVISTAMIENTOS);
    }
  };

  useEffect(() => {
    syncWithBackend();
  }, []);

  const handleDeptoSelect = (
    depto: string,
    setDeptoFn: (d: string) => void,
    setRegionFn: (r: string) => void
  ) => {
    setDeptoFn(depto);
    const match = DEPARTAMENTOS_REGIONES.find((d) => d.depto === depto);
    if (match) setRegionFn(match.region);
  };

  // --- HU-13: Registrar Avistamiento Diario ---
  const handleRegistrarAvistamiento = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatusBanner(null);

    if (!canWrite) {
      setStatusBanner({
        type: 'err',
        text: 'Debes iniciar sesión como Investigador o Científico para registrar avistamientos en campo.',
      });
      return;
    }

    const latVal = avLat.trim() === '' ? null : Number(avLat);
    const lonVal = avLon.trim() === '' ? null : Number(avLon);

    setLoading(true);
    const t0 = performance.now();
    const payload = {
      investigadorId: currentUser.id,
      especieId: avEspecieId,
      fechaHora: new Date().toISOString(),
      cantidad: Number(avCantidad),
      latitud: latVal,
      longitud: lonVal,
      departamento: avDepto,
      region: avRegion || null,
    };

    try {
      const saved = await registrarAvistamiento(payload);
      const elapsed = Math.round(performance.now() - t0);
      setBackendOnline(true);
      setAvistamientos((prev) => [saved, ...prev]);
      setStatusBanner({
        type: 'ok',
        text: `HU-13: Avistamiento registrado en servicio-registro (:8081) con ID ${saved.id.slice(
          0,
          8
        )} en ${elapsed} ms (< 2s RNF-10).`,
      });
    } catch (err) {
      // Fallback si el backend Spring Boot no está corriendo o error de validación
      const espObj = BACKEND_SEED_SPECIES.find((s) => s.id === avEspecieId);
      const elapsed = Math.round(performance.now() - t0);
      if (backendOnline && err instanceof Error) {
        setStatusBanner({ type: 'err', text: err.message });
      } else {
        const localSaved: AvistamientoResponse = {
          id: crypto.randomUUID(),
          especieId: avEspecieId,
          especie: espObj?.scientificName || 'Especie',
          fechaHora: payload.fechaHora,
          cantidad: payload.cantidad,
          latitud: payload.latitud,
          longitud: payload.longitud,
          departamento: payload.departamento,
          region: payload.region,
          creadoEn: new Date().toISOString(),
        };
        setAvistamientos((prev) => [localSaved, ...prev]);
        setStatusBanner({
          type: 'ok',
          text: `HU-13: Avistamiento registrado en estación base (${localSaved.id.slice(
            0,
            8
          )}) en ${elapsed} ms.`,
        });
      }
    } finally {
      setLoading(false);
    }
  };

  // --- HU-01 & HU-03: Registrar Hallazgo + Evidencias (HallazgoBuilder + EvidenciaFactory) ---
  const addEvidencia = (tipo: TipoEvidencia) => {
    setHaEvidencias((prev) => [
      ...prev,
      tipo === 'TEMPERATURA'
        ? {
            tipo: 'TEMPERATURA',
            valor: '26.4',
            unidad: 'C',
            atributoTaxonomico: 'Temperatura del agua',
          }
        : tipo === 'ADN'
        ? {
            tipo: 'ADN',
            valor: 'ADN-2026-550',
            atributoTaxonomico: 'Código de muestra genética',
          }
        : {
            tipo: 'AUDIO',
            urlArchivo: 'https://archivo.biohub.gov.co/acustica/canto-01.wav',
            atributoTaxonomico: 'Grabación acústica del canto',
          },
    ]);
  };

  const updateEvidencia = (index: number, patch: Partial<EvidenciaRequest>) => {
    setHaEvidencias((prev) =>
      prev.map((item, i) => (i === index ? { ...item, ...patch } : item))
    );
  };

  const removeEvidencia = (index: number) => {
    setHaEvidencias((prev) => prev.filter((_, i) => i !== index));
  };

  const handleRegistrarHallazgo = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatusBanner(null);

    if (!canWrite) {
      setStatusBanner({
        type: 'err',
        text: 'Acceso restringido: Inicia sesión como Investigador para registrar hallazgos biológicos.',
      });
      return;
    }

    // Validación idéntica a HallazgoBuilder.java: Latitud y longitud deben venir juntas o ninguna
    const hasLat = haLat.trim() !== '';
    const hasLon = haLon.trim() !== '';
    if (hasLat !== hasLon) {
      setStatusBanner({
        type: 'err',
        text: 'Validación HallazgoBuilder: Latitud y longitud deben venir juntas o ninguna.',
      });
      return;
    }

    // Validación idéntica a EvidenciaFactory.java
    for (const ev of haEvidencias) {
      if (ev.tipo === 'TEMPERATURA') {
        if (!ev.valor?.trim() || isNaN(Number(ev.valor))) {
          setStatusBanner({
            type: 'err',
            text: `EvidenciaFactory: El valor de la temperatura debe ser numérico (${
              ev.valor || 'vacío'
            }).`,
          });
          return;
        }
        if (!ev.unidad?.trim()) {
          setStatusBanner({
            type: 'err',
            text: 'EvidenciaFactory: La temperatura requiere una unidad (por ejemplo, C).',
          });
          return;
        }
      } else if (ev.tipo === 'ADN' && !ev.valor?.trim()) {
        setStatusBanner({
          type: 'err',
          text: 'EvidenciaFactory: La evidencia de ADN requiere el código de la muestra.',
        });
        return;
      } else if (ev.tipo === 'AUDIO' && !ev.urlArchivo?.trim()) {
        setStatusBanner({
          type: 'err',
          text: 'EvidenciaFactory: La evidencia de audio requiere la URL del archivo.',
        });
        return;
      }
    }

    setLoading(true);
    const payload = {
      investigadorId: currentUser.id,
      especieId: haEspecieId,
      fichaId: haFichaId.trim() || undefined,
      fecha: new Date().toISOString(),
      descripcion: haDescripcion.trim(),
      latitud: hasLat ? Number(haLat) : null,
      longitud: hasLon ? Number(haLon) : null,
      departamento: haDepto,
      region: haRegion || null,
      evidencias: haEvidencias,
    };

    const espMeta =
      BACKEND_SEED_SPECIES.find((s) => s.id === haEspecieId) ||
      BACKEND_SEED_SPECIES[0];

    try {
      const result = await registrarHallazgo(payload);
      setBackendOnline(true);
      const created: HallazgoResponse = result.hallazgo || {
        id: result.id || crypto.randomUUID(),
        investigadorId: payload.investigadorId,
        especieId: payload.especieId,
        especie: espMeta.scientificName,
        fichaId: payload.fichaId || null,
        fecha: payload.fecha,
        descripcion: payload.descripcion,
        latitud: payload.latitud,
        longitud: payload.longitud,
        departamento: payload.departamento,
        region: payload.region,
        versionActual: 1,
        creadoEn: new Date().toISOString(),
        evidencias: payload.evidencias.map((ev, i) => ({
          id: `ev-${i}`,
          tipo: ev.tipo,
          valor: ev.valor || null,
          unidad: ev.unidad || null,
          urlArchivo: ev.urlArchivo || null,
          atributoTaxonomico: ev.atributoTaxonomico || null,
        })),
      };

      setHallazgos((prev) => [created, ...prev]);
      setTargetHallazgoId(created.id);
      setHaDescripcion('');

      // Publicar también en la vitrina de fichas taxonómicas
      onNewSpeciesCardCreated({
        id: created.id.slice(0, 8).toUpperCase(),
        commonName: espMeta.commonName,
        scientificName: espMeta.scientificName,
        status: espMeta.status,
        specimenCode: created.fichaId || 'EST-CAMPO-2026',
        illustrationVariant:
          espMeta.status === 'CR'
            ? 'reptile'
            : espMeta.status === 'LC'
            ? 'avian'
            : 'amphibian',
        attributes: [
          {
            label: 'Ubicación registrada',
            value: `${created.departamento} (${created.region || 'Colombia'})`,
          },
          ...created.evidencias.map((ev) => ({
            label: ev.atributoTaxonomico || `Evidencia ${ev.tipo}`,
            value:
              ev.tipo === 'TEMPERATURA'
                ? `${ev.valor} ${ev.unidad}`
                : ev.tipo === 'ADN'
                ? `${ev.valor}`
                : `${ev.urlArchivo}`,
          })),
        ],
      });

      setStatusBanner({
        type: 'ok',
        text: `HU-01 / HU-03: Hallazgo registrado en MySQL (:8081) con UUID ${created.id} (v1) y ${created.evidencias.length} evidencias validadas.`,
      });
    } catch (err) {
      if (backendOnline && err instanceof Error) {
        setStatusBanner({ type: 'err', text: err.message });
      } else {
        const localCreated: HallazgoResponse = {
          id: crypto.randomUUID(),
          investigadorId: payload.investigadorId,
          especieId: payload.especieId,
          especie: espMeta.scientificName,
          fichaId: payload.fichaId || null,
          fecha: payload.fecha,
          descripcion: payload.descripcion,
          latitud: payload.latitud,
          longitud: payload.longitud,
          departamento: payload.departamento,
          region: payload.region,
          versionActual: 1,
          creadoEn: new Date().toISOString(),
          evidencias: payload.evidencias.map((ev, i) => ({
            id: `ev-local-${i}`,
            tipo: ev.tipo,
            valor: ev.valor || null,
            unidad: ev.unidad || null,
            urlArchivo: ev.urlArchivo || null,
            atributoTaxonomico: ev.atributoTaxonomico || null,
          })),
        };
        setHallazgos((prev) => [localCreated, ...prev]);
        setTargetHallazgoId(localCreated.id);
        setHaDescripcion('');

        onNewSpeciesCardCreated({
          id: localCreated.id.slice(0, 8).toUpperCase(),
          commonName: espMeta.commonName,
          scientificName: espMeta.scientificName,
          status: espMeta.status,
          specimenCode: localCreated.fichaId || 'EST-CAMPO-2026',
          illustrationVariant:
            espMeta.status === 'CR'
              ? 'reptile'
              : espMeta.status === 'LC'
              ? 'avian'
              : 'amphibian',
          attributes: [
            {
              label: 'Ubicación registrada',
              value: `${localCreated.departamento} (${localCreated.region || 'Colombia'})`,
            },
            ...localCreated.evidencias.map((ev) => ({
              label: ev.atributoTaxonomico || `Evidencia ${ev.tipo}`,
              value:
                ev.tipo === 'TEMPERATURA'
                  ? `${ev.valor} ${ev.unidad}`
                  : ev.tipo === 'ADN'
                  ? `${ev.valor}`
                  : `${ev.urlArchivo}`,
            })),
          ],
        });

        setStatusBanner({
          type: 'ok',
          text: `HU-01 / HU-03: Hallazgo registrado (${localCreated.id.slice(
            0,
            8
          )}) con ${localCreated.evidencias.length} evidencias y agregado a las fichas taxonómicas.`,
        });
      }
    } finally {
      setLoading(false);
    }
  };

  // --- HU-05: Buscar Hallazgo por UUID en GET /hallazgos/{id} ---
  const handleBuscarHallazgoUuid = async () => {
    if (!lookupUuid.trim()) return;
    setStatusBanner(null);
    setLoading(true);
    try {
      const found = await obtenerHallazgoPorId(lookupUuid.trim());
      setBackendOnline(true);
      setHallazgos((prev) => {
        const exists = prev.some((h) => h.id === found.id);
        return exists
          ? prev.map((h) => (h.id === found.id ? found : h))
          : [found, ...prev];
      });
      setTargetHallazgoId(found.id);
      setStatusBanner({
        type: 'ok',
        text: `Hallazgo ${found.id} recuperado desde GET /hallazgos/{id} (v${found.versionActual}).`,
      });
    } catch (err) {
      setStatusBanner({
        type: 'err',
        text:
          err instanceof Error
            ? err.message
            : 'No se encontró el UUID en servicio-registro (:8081).',
      });
    } finally {
      setLoading(false);
    }
  };

  // --- HU-19.1: Modificar Hallazgo (PATCH /hallazgos/{id}) ---
  const handleModificarHallazgo = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatusBanner(null);
    const current = hallazgos.find((h) => h.id === targetHallazgoId);
    if (!current) return;

    const prevValue =
      modCampo === 'descripcion'
        ? current.descripcion
        : modCampo === 'departamento'
        ? current.departamento
        : current.region || '';

    if (prevValue === modValor.trim()) {
      setStatusBanner({
        type: 'err',
        text: 'DatosInvalidosException: El nuevo valor es igual al actual.',
      });
      return;
    }

    setLoading(true);
    try {
      const updated = await modificarHallazgo(targetHallazgoId, {
        campo: modCampo,
        valor: modValor.trim(),
        autor: currentUser.id,
      });
      setBackendOnline(true);
      setHallazgos((prev) =>
        prev.map((h) => (h.id === updated.id ? updated : h))
      );
      onNewHistoryEventAdded(
        {
          id: `EVT-${Date.now().toString().slice(-4)}`,
          version: `v${updated.versionActual}`,
          date: new Date().toISOString().replace('T', ' · ').slice(0, 18) + ' UTC',
          previousBelief: `${modCampo}: "${prevValue}"`,
          currentKnowledge: `${modCampo}: "${modValor.trim()}"`,
          authorizedBy: `${currentUser.name} (${currentUser.id})`,
          reason: `Modificación de atributo ${modCampo} vía PATCH /hallazgos/{id}`,
        },
        updated.id.slice(0, 8).toUpperCase()
      );
      setModValor('');
      setStatusBanner({
        type: 'ok',
        text: `HU-19.1: Hallazgo modificado a versión v${updated.versionActual} y evento añadido a la línea de tiempo.`,
      });
    } catch (err) {
      if (backendOnline && err instanceof Error) {
        setStatusBanner({ type: 'err', text: err.message });
      } else {
        const nextVersion = current.versionActual + 1;
        const localUpdated: HallazgoResponse = {
          ...current,
          [modCampo]: modValor.trim(),
          versionActual: nextVersion,
        };
        setHallazgos((prev) =>
          prev.map((h) => (h.id === current.id ? localUpdated : h))
        );
        onNewHistoryEventAdded(
          {
            id: `EVT-${Date.now().toString().slice(-4)}`,
            version: `v${nextVersion}`,
            date: new Date().toISOString().replace('T', ' · ').slice(0, 18) + ' UTC',
            previousBelief: `${modCampo}: "${prevValue}"`,
            currentKnowledge: `${modCampo}: "${modValor.trim()}"`,
            authorizedBy: `${currentUser.name} (${currentUser.id})`,
            reason: `Actualización trazable del campo ${modCampo} (HU-19.1)`,
          },
          current.id.slice(0, 8).toUpperCase()
        );
        setModValor('');
        setStatusBanner({
          type: 'ok',
          text: `HU-19.1: Hallazgo actualizado a versión v${nextVersion} y registrado en el historial inmutable.`,
        });
      }
    } finally {
      setLoading(false);
    }
  };

  // --- HU-10 & HU-19.1: Reclasificar Hallazgo (POST /hallazgos/{id}/reclasificacion) ---
  const handleReclasificarHallazgo = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatusBanner(null);
    const current = hallazgos.find((h) => h.id === targetHallazgoId);
    if (!current) return;

    if (current.especieId === recNuevaEspecieId) {
      setStatusBanner({
        type: 'err',
        text: 'DatosInvalidosException: El hallazgo ya está clasificado como esa especie.',
      });
      return;
    }

    const nuevaEsp = BACKEND_SEED_SPECIES.find((s) => s.id === recNuevaEspecieId);
    if (!nuevaEsp) return;

    setLoading(true);
    try {
      const updated = await reclasificarHallazgo(targetHallazgoId, {
        nuevaEspecieId: recNuevaEspecieId,
        motivo: recMotivo.trim(),
        autor: currentUser.id,
        autorizador: recAutorizador,
      });
      setBackendOnline(true);
      setHallazgos((prev) =>
        prev.map((h) => (h.id === updated.id ? updated : h))
      );
      onNewHistoryEventAdded(
        {
          id: `EVT-${Date.now().toString().slice(-4)}`,
          version: `v${updated.versionActual}`,
          date: new Date().toISOString().replace('T', ' · ').slice(0, 18) + ' UTC',
          previousBelief: current.especie,
          currentKnowledge: updated.especie,
          authorizedBy: `Autor: ${currentUser.id} · Autorizó: ${recAutorizador}`,
          reason: recMotivo.trim(),
        },
        updated.id.slice(0, 8).toUpperCase()
      );
      setRecMotivo('');
      setStatusBanner({
        type: 'ok',
        text: `HU-10 / HU-19.1: Reclasificación científica aplicada (${current.especie} → ${updated.especie}, v${updated.versionActual}).`,
      });
    } catch (err) {
      if (backendOnline && err instanceof Error) {
        setStatusBanner({ type: 'err', text: err.message });
      } else {
        const nextVersion = current.versionActual + 1;
        const localUpdated: HallazgoResponse = {
          ...current,
          especieId: nuevaEsp.id,
          especie: nuevaEsp.scientificName,
          versionActual: nextVersion,
        };
        setHallazgos((prev) =>
          prev.map((h) => (h.id === current.id ? localUpdated : h))
        );
        onNewHistoryEventAdded(
          {
            id: `EVT-${Date.now().toString().slice(-4)}`,
            version: `v${nextVersion}`,
            date: new Date().toISOString().replace('T', ' · ').slice(0, 18) + ' UTC',
            previousBelief: current.especie,
            currentKnowledge: nuevaEsp.scientificName,
            authorizedBy: `Autor: ${currentUser.id} · Autorizó: ${recAutorizador}`,
            reason: recMotivo.trim(),
          },
          current.id.slice(0, 8).toUpperCase()
        );
        setRecMotivo('');
        setStatusBanner({
          type: 'ok',
          text: `HU-10 / HU-19.1: Reclasificación científica registrada (${current.especie} → ${nuevaEsp.scientificName}, v${nextVersion}) y añadida a la línea de tiempo.`,
        });
      }
    } finally {
      setLoading(false);
    }
  };

  const selectedHallazgo = hallazgos.find((h) => h.id === targetHallazgoId);

  return (
    <div className="space-y-8">
      {/* Barra de estado de estación de campo + Conexión con servicio-registro (:8081) */}
      <Card className="flex flex-wrap items-center justify-between gap-4 bg-surface">
        <div className="space-y-1">
          <div className="flex flex-wrap items-center gap-2">
            <span
              className={`h-2.5 w-2.5 rounded-full ${
                backendOnline ? 'bg-iucn-lc' : 'bg-sun-500'
              }`}
              aria-hidden="true"
            />
            <span className="font-mono text-xs font-semibold text-ink">
              {backendOnline
                ? 'CONECTADO EN VIVO A SERVICIO-REGISTRO (MySQL :8081)'
                : 'MODO ESTACIÓN LOCAL (Sincronizable con :8081)'}
            </span>
            <span className="font-mono text-xs text-ink-muted">
              · Usuario activo: {currentUser.name} ({currentUser.roleLabel})
            </span>
          </div>
          <p className="text-xs text-ink-muted">
            Historias de usuario operativas en este módulo:{' '}
            <strong className="text-ink">
              HU-01 (Registrar hallazgos), HU-03 (Registrar evidencias), HU-10 (Responsable
              y autorizador), HU-13 (Avistamientos diarios) y HU-19.1 (Modificar y
              reclasificar con versionado)
            </strong>
            .
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Button variant="ghost" onClick={syncWithBackend} className="text-xs py-1.5">
            Sincronizar :8081
          </Button>
          {!canWrite && (
            <Button variant="secondary" onClick={onOpenAuth} className="text-xs py-1.5">
              Iniciar sesión para registrar
            </Button>
          )}
        </div>
      </Card>

      {/* Selector de las 3 operaciones implementadas en servicio-registro */}
      <div
        role="tablist"
        aria-label="Operaciones de estación de campo"
        className="flex flex-wrap gap-2 border-b border-line pb-3"
      >
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'avistamientos'}
          onClick={() => {
            setActiveTab('avistamientos');
            setStatusBanner(null);
          }}
          className={`rounded-md px-4 py-2 font-mono text-xs font-medium transition-colors cursor-pointer ${
            activeTab === 'avistamientos'
              ? 'bg-forest-900 text-white'
              : 'bg-surface text-ink border border-line hover:border-forest-700'
          }`}
        >
          1. Avistamientos diarios (HU-13 · POST / GET /avistamientos)
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'hallazgos'}
          onClick={() => {
            setActiveTab('hallazgos');
            setStatusBanner(null);
          }}
          className={`rounded-md px-4 py-2 font-mono text-xs font-medium transition-colors cursor-pointer ${
            activeTab === 'hallazgos'
              ? 'bg-forest-900 text-white'
              : 'bg-surface text-ink border border-line hover:border-forest-700'
          }`}
        >
          2. Registrar hallazgo y evidencias (HU-01 / HU-03 · POST /hallazgos)
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'reclasificacion'}
          onClick={() => {
            setActiveTab('reclasificacion');
            setStatusBanner(null);
          }}
          className={`rounded-md px-4 py-2 font-mono text-xs font-medium transition-colors cursor-pointer ${
            activeTab === 'reclasificacion'
              ? 'bg-forest-900 text-white'
              : 'bg-surface text-ink border border-line hover:border-forest-700'
          }`}
        >
          3. Modificar y reclasificar especie (HU-10 / HU-19.1 · PATCH & POST)
        </button>
      </div>

      {/* Mensaje de confirmación o error de validación */}
      {statusBanner && (
        <div
          role="alert"
          className={`rounded-lg border p-4 text-xs font-mono ${
            statusBanner.type === 'ok'
              ? 'border-iucn-lc bg-iucn-lc/10 text-ink'
              : 'border-iucn-cr bg-iucn-cr/10 text-ink'
          }`}
        >
          {statusBanner.text}
        </div>
      )}

      {/* PESTAÑA 1: AVISTAMIENTOS DIARIOS (HU-13) */}
      {activeTab === 'avistamientos' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          <Card as="section" className="lg:col-span-5 space-y-4">
            <div className="border-b border-line pb-3">
              <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                HU-13 · Registro ágil independiente (&lt; 2s)
              </span>
              <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
                Nuevo avistamiento diario
              </h3>
            </div>

            <form onSubmit={handleRegistrarAvistamiento} className="space-y-3.5">
              <div>
                <label className="block text-xs font-medium text-ink mb-1">
                  Investigador responsable (`investigadorId`)
                </label>
                <input
                  type="text"
                  readOnly
                  value={currentUser.id}
                  className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink-muted"
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div className="col-span-2">
                  <label className="block text-xs font-medium text-ink mb-1">
                    Especie observada (`especieId`)
                  </label>
                  <select
                    value={avEspecieId}
                    onChange={(e) => setAvEspecieId(e.target.value)}
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  >
                    {BACKEND_SEED_SPECIES.map((sp) => (
                      <option key={sp.id} value={sp.id}>
                        {sp.commonName} ({sp.scientificName}) [{sp.status}]
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Individuos
                  </label>
                  <input
                    type="number"
                    min={1}
                    required
                    value={avCantidad}
                    onChange={(e) => setAvCantidad(Number(e.target.value))}
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Departamento
                  </label>
                  <select
                    value={avDepto}
                    onChange={(e) =>
                      handleDeptoSelect(e.target.value, setAvDepto, setAvRegion)
                    }
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  >
                    {DEPARTAMENTOS_REGIONES.map((d) => (
                      <option key={d.depto} value={d.depto}>
                        {d.depto}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Región natural
                  </label>
                  <input
                    type="text"
                    value={avRegion}
                    onChange={(e) => setAvRegion(e.target.value)}
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Latitud (-90 a 90)
                  </label>
                  <input
                    type="number"
                    step="any"
                    min={-90}
                    max={90}
                    value={avLat}
                    onChange={(e) => setAvLat(e.target.value)}
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Longitud (-180 a 180)
                  </label>
                  <input
                    type="number"
                    step="any"
                    min={-180}
                    max={180}
                    value={avLon}
                    onChange={(e) => setAvLon(e.target.value)}
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                  />
                </div>
              </div>

              <Button
                type="submit"
                variant="primary"
                disabled={loading}
                className="w-full"
              >
                {loading ? 'Guardando avistamiento...' : 'Guardar avistamiento diario'}
              </Button>
            </form>
          </Card>

          <Card as="section" className="lg:col-span-7 space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line pb-3">
              <div>
                <span className="font-mono text-xs uppercase tracking-wider text-ink-muted">
                  GET /avistamientos · Orden cronológico descendente
                </span>
                <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
                  Bitácora de avistamientos registrados ({avistamientos.length})
                </h3>
              </div>
              <span className="font-mono text-xs text-moss-500 font-semibold">
                HU-13
              </span>
            </div>

            <div className="space-y-3 max-h-96 overflow-y-auto pr-1">
              {avistamientos.map((av) => {
                const spMeta = BACKEND_SEED_SPECIES.find((s) => s.id === av.especieId);
                return (
                  <div
                    key={av.id}
                    className="rounded-lg border border-line bg-paper p-3.5 flex flex-wrap items-center justify-between gap-3"
                  >
                    <div className="space-y-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="font-serif italic font-semibold text-base text-ink">
                          {av.especie}
                        </span>
                        {spMeta && <ConservationBadge status={spMeta.status} />}
                        <span className="rounded bg-forest-900 px-2 py-0.5 font-mono text-xs text-white">
                          {av.cantidad} ind.
                        </span>
                      </div>
                      <p className="text-xs text-ink-muted">
                        {av.departamento} {av.region ? `· Región ${av.region}` : ''}{' '}
                        {av.latitud !== null && av.longitud !== null && (
                          <span className="font-mono">
                            ({av.latitud.toFixed(4)}, {av.longitud.toFixed(4)})
                          </span>
                        )}
                      </p>
                    </div>
                    <div className="text-right font-mono text-xs text-ink-muted">
                      <p className="text-ink">
                        {new Date(av.fechaHora).toLocaleString('es-CO')}
                      </p>
                      <p className="text-[11px]">ID: {av.id.slice(0, 12)}...</p>
                    </div>
                  </div>
                );
              })}
            </div>
          </Card>
        </div>
      )}

      {/* PESTAÑA 2: REGISTRAR HALLAZGO + EVIDENCIAS (HU-01 & HU-03) */}
      {activeTab === 'hallazgos' && (
        <Card as="section" className="space-y-6">
          <div className="flex flex-wrap items-baseline justify-between gap-2 border-b border-line pb-4">
            <div>
              <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                HU-01 & HU-03 · Patrones HallazgoBuilder + EvidenciaFactory
              </span>
              <h3 className="mt-1 font-serif text-2xl font-semibold text-ink">
                Registrar hallazgo biológico con evidencias científicas
              </h3>
            </div>
            <span className="font-mono text-xs text-ink-muted">
              POST /hallazgos · Responsable: {currentUser.id}
            </span>
          </div>

          <form onSubmit={handleRegistrarHallazgo} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-xs font-medium text-ink mb-1">
                  Especie identificada (`especieId`)
                </label>
                <select
                  value={haEspecieId}
                  onChange={(e) => setHaEspecieId(e.target.value)}
                  className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                >
                  {BACKEND_SEED_SPECIES.map((sp) => (
                    <option key={sp.id} value={sp.id}>
                      {sp.commonName} — {sp.scientificName} [{sp.status}]
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-ink mb-1">
                  Código de ficha taxonómica (`fichaId`)
                </label>
                <input
                  type="text"
                  value={haFichaId}
                  onChange={(e) => setHaFichaId(e.target.value)}
                  placeholder="FICHA-TOMO-2026"
                  className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Departamento
                  </label>
                  <select
                    value={haDepto}
                    onChange={(e) =>
                      handleDeptoSelect(e.target.value, setHaDepto, setHaRegion)
                    }
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  >
                    {DEPARTAMENTOS_REGIONES.map((d) => (
                      <option key={d.depto} value={d.depto}>
                        {d.depto}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Región
                  </label>
                  <input
                    type="text"
                    value={haRegion}
                    onChange={(e) => setHaRegion(e.target.value)}
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  />
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="md:col-span-2">
                <label className="block text-xs font-medium text-ink mb-1">
                  Descripción científica del hallazgo (máx. 1000 caracteres)
                </label>
                <textarea
                  rows={3}
                  maxLength={1000}
                  required
                  value={haDescripcion}
                  onChange={(e) => setHaDescripcion(e.target.value)}
                  placeholder="Describe el ejemplar, microhábitat, comportamiento observado y condiciones de muestreo..."
                  className="w-full rounded-md border border-line bg-paper px-3 py-2 text-sm text-ink"
                />
              </div>

              <div className="rounded-lg border border-line bg-paper p-3.5 space-y-2">
                <p className="font-mono text-[11px] uppercase tracking-wider text-ink-muted">
                  Coordenadas GPS (ambas o ninguna)
                </p>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-[11px] text-ink-muted mb-1">
                      Latitud
                    </label>
                    <input
                      type="number"
                      step="any"
                      value={haLat}
                      onChange={(e) => setHaLat(e.target.value)}
                      className="w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] text-ink-muted mb-1">
                      Longitud
                    </label>
                    <input
                      type="number"
                      step="any"
                      value={haLon}
                      onChange={(e) => setHaLon(e.target.value)}
                      className="w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* Constructor de Evidencias (EvidenciaFactory: TEMPERATURA, ADN, AUDIO) */}
            <div className="space-y-3 border-t border-line pt-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <h4 className="font-serif text-lg font-semibold text-ink">
                    Evidencias vinculadas al hallazgo (HU-03)
                  </h4>
                  <p className="text-xs text-ink-muted">
                    Cada evidencia es validada por <code className="font-mono">EvidenciaFactory</code> según su tipo:
                  </p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Button
                    variant="ghost"
                    onClick={() => addEvidencia('TEMPERATURA')}
                    className="text-xs py-1.5 px-3"
                  >
                    + Temperatura
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => addEvidencia('ADN')}
                    className="text-xs py-1.5 px-3"
                  >
                    + Muestra ADN
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => addEvidencia('AUDIO')}
                    className="text-xs py-1.5 px-3"
                  >
                    + Audio bioacústico
                  </Button>
                </div>
              </div>

              <div className="space-y-2.5">
                {haEvidencias.map((ev, idx) => (
                  <div
                    key={idx}
                    className="grid grid-cols-1 md:grid-cols-12 gap-3 items-end rounded-lg border border-line bg-paper p-3"
                  >
                    <div className="md:col-span-2">
                      <label className="block text-[11px] text-ink-muted mb-1">
                        Tipo (`TipoEvidencia`)
                      </label>
                      <select
                        value={ev.tipo}
                        onChange={(e) =>
                          updateEvidencia(idx, {
                            tipo: e.target.value as TipoEvidencia,
                          })
                        }
                        className="w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                      >
                        <option value="TEMPERATURA">TEMPERATURA</option>
                        <option value="ADN">ADN</option>
                        <option value="AUDIO">AUDIO</option>
                      </select>
                    </div>

                    {ev.tipo === 'TEMPERATURA' && (
                      <>
                        <div className="md:col-span-3">
                          <label className="block text-[11px] text-ink-muted mb-1">
                            Valor numérico
                          </label>
                          <input
                            type="text"
                            value={ev.valor || ''}
                            onChange={(e) =>
                              updateEvidencia(idx, { valor: e.target.value })
                            }
                            placeholder="28.5"
                            className="w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                          />
                        </div>
                        <div className="md:col-span-2">
                          <label className="block text-[11px] text-ink-muted mb-1">
                            Unidad
                          </label>
                          <input
                            type="text"
                            value={ev.unidad || ''}
                            onChange={(e) =>
                              updateEvidencia(idx, { unidad: e.target.value })
                            }
                            placeholder="C"
                            className="w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                          />
                        </div>
                      </>
                    )}

                    {ev.tipo === 'ADN' && (
                      <div className="md:col-span-5">
                        <label className="block text-[11px] text-ink-muted mb-1">
                          Código de muestra de ADN (`valor`)
                        </label>
                        <input
                          type="text"
                          value={ev.valor || ''}
                          onChange={(e) =>
                            updateEvidencia(idx, { valor: e.target.value })
                          }
                          placeholder="ADN-2026-901"
                          className="w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                        />
                      </div>
                    )}

                    {ev.tipo === 'AUDIO' && (
                      <div className="md:col-span-5">
                        <label className="block text-[11px] text-ink-muted mb-1">
                          URL del archivo acústico (`urlArchivo`)
                        </label>
                        <input
                          type="text"
                          value={ev.urlArchivo || ''}
                          onChange={(e) =>
                            updateEvidencia(idx, { urlArchivo: e.target.value })
                          }
                          placeholder="https://archivo.biohub.gov.co/canto.wav"
                          className="w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                        />
                      </div>
                    )}

                    <div className="md:col-span-4">
                      <label className="block text-[11px] text-ink-muted mb-1">
                        Atributo taxonómico (`atributoTaxonomico`)
                      </label>
                      <input
                        type="text"
                        value={ev.atributoTaxonomico || ''}
                        onChange={(e) =>
                          updateEvidencia(idx, {
                            atributoTaxonomico: e.target.value,
                          })
                        }
                        placeholder="Temperatura del agua"
                        className="w-full rounded border border-line bg-surface px-2.5 py-1.5 text-xs text-ink"
                      />
                    </div>

                    <div className="md:col-span-1 flex justify-end">
                      <button
                        type="button"
                        onClick={() => removeEvidencia(idx)}
                        className="rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-iucn-cr hover:border-iucn-cr cursor-pointer"
                        aria-label="Eliminar evidencia"
                      >
                        ✕
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex justify-end">
              <Button type="submit" variant="primary" disabled={loading}>
                {loading
                  ? 'Registrando hallazgo...'
                  : 'Registrar hallazgo y publicar ficha'}
              </Button>
            </div>
          </form>
        </Card>
      )}

      {/* PESTAÑA 3: MODIFICAR Y RECLASIFICAR HALLAZGO CON VERSIONADO (HU-10 & HU-19.1) */}
      {activeTab === 'reclasificacion' && (
        <div className="space-y-6">
          <Card className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-end">
              <div className="md:col-span-7">
                <label className="block text-xs font-medium text-ink mb-1">
                  Seleccionar hallazgo registrado para modificar o reclasificar
                </label>
                <select
                  value={targetHallazgoId}
                  onChange={(e) => setTargetHallazgoId(e.target.value)}
                  className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                >
                  {hallazgos.map((h) => (
                    <option key={h.id} value={h.id}>
                      [v{h.versionActual}] {h.especie} — {h.departamento} ({h.id})
                    </option>
                  ))}
                </select>
              </div>

              <div className="md:col-span-5 flex gap-2">
                <input
                  type="text"
                  value={lookupUuid}
                  onChange={(e) => setLookupUuid(e.target.value)}
                  placeholder="Consultar UUID en GET /hallazgos/{id}"
                  className="flex-1 rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                />
                <Button
                  variant="ghost"
                  onClick={handleBuscarHallazgoUuid}
                  className="text-xs py-2"
                >
                  Buscar UUID
                </Button>
              </div>
            </div>

            {selectedHallazgo && (
              <div className="rounded-lg border border-line bg-paper p-3.5 grid grid-cols-1 md:grid-cols-3 gap-3 text-xs">
                <div>
                  <span className="text-ink-muted block">Especie y versión vigente:</span>
                  <span className="font-serif italic font-semibold text-ink">
                    {selectedHallazgo.especie}
                  </span>{' '}
                  <span className="font-mono font-bold text-moss-500">
                    (v{selectedHallazgo.versionActual})
                  </span>
                </div>
                <div>
                  <span className="text-ink-muted block">Ubicación registrada:</span>
                  <span className="font-mono text-ink">
                    {selectedHallazgo.departamento} · {selectedHallazgo.region || 'Sin región'}
                  </span>
                </div>
                <div>
                  <span className="text-ink-muted block">Descripción actual:</span>
                  <span className="text-ink line-clamp-1">
                    {selectedHallazgo.descripcion}
                  </span>
                </div>
              </div>
            )}
          </Card>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* PATCH /hallazgos/{id} */}
            <Card as="section" className="space-y-4">
              <div className="border-b border-line pb-3">
                <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                  HU-19.1 · PATCH /hallazgos/&#123;id&#125;
                </span>
                <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
                  Modificar atributo con versionado
                </h3>
              </div>

              <form onSubmit={handleModificarHallazgo} className="space-y-3.5">
                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Campo permitido (`descripcion`, `departamento`, `region`)
                  </label>
                  <select
                    value={modCampo}
                    onChange={(e) =>
                      setModCampo(
                        e.target.value as 'descripcion' | 'departamento' | 'region'
                      )
                    }
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                  >
                    <option value="descripcion">descripcion (máx. 1000 caracteres)</option>
                    <option value="departamento">departamento (máx. 80 caracteres)</option>
                    <option value="region">region (máx. 80 caracteres)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Nuevo valor (debe ser distinto al actual)
                  </label>
                  <textarea
                    rows={3}
                    required
                    value={modValor}
                    onChange={(e) => setModValor(e.target.value)}
                    placeholder={`Ingresa el nuevo valor para ${modCampo}...`}
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Autor de la modificación (`autor`)
                  </label>
                  <input
                    type="text"
                    readOnly
                    value={currentUser.id}
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink-muted"
                  />
                </div>

                <Button
                  type="submit"
                  variant="primary"
                  disabled={loading}
                  className="w-full"
                >
                  Aplicar cambio e incrementar versión
                </Button>
              </form>
            </Card>

            {/* POST /hallazgos/{id}/reclasificacion */}
            <Card as="section" className="space-y-4">
              <div className="border-b border-line pb-3">
                <span className="font-mono text-xs uppercase tracking-wider text-sun-500 font-semibold">
                  HU-10 & HU-19.1 · POST /hallazgos/&#123;id&#125;/reclasificacion
                </span>
                <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
                  Reclasificar especie con autorizador
                </h3>
              </div>

              <form onSubmit={handleReclasificarHallazgo} className="space-y-3.5">
                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Nueva especie (`nuevaEspecieId`)
                  </label>
                  <select
                    value={recNuevaEspecieId}
                    onChange={(e) => setRecNuevaEspecieId(e.target.value)}
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  >
                    {BACKEND_SEED_SPECIES.map((sp) => (
                      <option key={sp.id} value={sp.id}>
                        {sp.scientificName} ({sp.commonName}) [{sp.status}]
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-medium text-ink mb-1">
                    Motivo científico de reclasificación (`motivo` · máx. 500 chars)
                  </label>
                  <textarea
                    rows={2}
                    maxLength={500}
                    required
                    value={recMotivo}
                    onChange={(e) => setRecMotivo(e.target.value)}
                    placeholder="Ej. Análisis genético de muestra ADN confirma nueva clasificación taxonómica..."
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-ink mb-1">
                      Autor (`autor`)
                    </label>
                    <input
                      type="text"
                      readOnly
                      value={currentUser.id}
                      className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink-muted"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-ink mb-1">
                      Científico autorizador (`autorizador`)
                    </label>
                    <select
                      value={recAutorizador}
                      onChange={(e) => setRecAutorizador(e.target.value)}
                      className="w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                    >
                      <option value="alt-escobar-02">
                        alt-escobar-02 (Dra. Elena Escobar)
                      </option>
                      <option value="dir-rodriguez-04">
                        dir-rodriguez-04 (Dr. Juan D. Rodríguez)
                      </option>
                    </select>
                  </div>
                </div>

                <Button
                  type="submit"
                  variant="secondary"
                  disabled={loading}
                  className="w-full"
                >
                  {canAuthorizeReclassification(currentUser.role)
                    ? 'Autorizar y ejecutar reclasificación'
                    : 'Solicitar y registrar reclasificación científica'}
                </Button>
              </form>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
}
