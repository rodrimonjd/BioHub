'use client';

import React, { useState, useEffect, useRef } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Field } from '@/components/ui/Field';
import { ErrorFormulario } from '@/components/ui/ErrorFormulario';
import { useNotificar } from '@/components/ui/Notificaciones';
import { ConservationBadge } from '@/components/ui/ConservationBadge';
import {
  BioHubUser,
  canRegisterFieldData,
  canAuthorizeReclassification,
  canEditFindings,
  canManageEquipment,
  PERMISOS,
} from '@/lib/permissions';
import { useAccesoUbicacion } from '@/lib/useAccesoUbicacion';
import { avisoDeError } from '@/lib/avisos';
import { coincideHallazgo, nombreComunHallazgo } from '@/lib/hallazgos';
import { FichaTaxonomica } from '@/lib/taxonomia-api';
import { listarUsuariosConPermiso, ResumenUsuario } from '@/lib/autorizacion-api';
import {
  AvistamientoRequest,
  AvistamientoResponse,
  BACKEND_SEED_SPECIES,
  ErrorRechazado,
  EvidenciaRequest,
  Expedicion,
  listarExpediciones,
  HallazgoResponse,
  listarAvistamientos,
  listarHallazgos,
  modificarHallazgo,
  reclasificarHallazgo,
  registrarAvistamiento,
  registrarHallazgo,
  SeedSpecies,
  TipoEvidencia,
} from '@/lib/registro-api';
import { SpeciesCardProps } from './SpeciesCard';
import { GestionCampo } from './GestionCampo';
import { AgregarEvidencia } from './AgregarEvidencia';
import { NuevaEspecie } from './NuevaEspecie';
import { ListaHallazgos } from './ListaHallazgos';
import { HistoryEventItem } from './HistoryTimeline';

export interface FieldStationWorkspaceProps {
  currentUser: BioHubUser;
  onNewSpeciesCardCreated: (card: SpeciesCardProps) => void;
  onNewHistoryEventAdded: (event: HistoryEventItem, findingId: string) => void;
  /** Catálogo de especies de servicio-registro (GET /especies). Vacío o ausente: catálogo base sin conexión. */
  especies?: SeedSpecies[];
  /** Fichas de servicio-taxonomia (GET /fichas), para asociar la ficha al registrar un hallazgo. */
  fichas?: FichaTaxonomica[];
  /** Tras agregar una especie, el panel recarga el catálogo desde la base de datos. */
  onEspecieCreada?: () => Promise<void> | void;
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
  {
    id: 'c441f820-9012-4110-8100-445566778899',
    especieId: '33333333-3333-3333-3333-333333333333',
    especie: 'Ara macao',
    fechaHora: '2026-11-20T16:20:00Z',
    cantidad: 8,
    latitud: -1.1984,
    longitud: -71.5432,
    departamento: 'Amazonas',
    region: 'Amazonía',
    creadoEn: '2026-11-20T16:20:02Z',
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

const PAGE_SIZE = 3;

export function FieldStationWorkspace({
  currentUser,
  onNewSpeciesCardCreated,
  onNewHistoryEventAdded,
  especies,
  fichas,
  onEspecieCreada,
}: FieldStationWorkspaceProps) {
  // Especies de la base de datos; si registro no responde, el catálogo base para trabajar sin conexión
  const catalogo = especies && especies.length > 0 ? especies : BACKEND_SEED_SPECIES;
  const [activeTab, setActiveTab] = useState<
    'avistamientos' | 'hallazgos' | 'reclasificacion' | 'gestion'
  >('avistamientos');
  const notificar = useNotificar();
  // Errores de un dato del formulario; los demás avisos son flotantes
  const [errorFormulario, setErrorFormulario] = useState<{
    formulario: 'hallazgo' | 'modificar' | 'reclasificar';
    texto: string;
  } | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  // Bitácora con paginación incremental
  const [avistamientos, setAvistamientos] = useState<AvistamientoResponse[]>(
    INITIAL_LOCAL_AVISTAMIENTOS
  );
  const [visibleCount, setVisibleCount] = useState<number>(PAGE_SIZE);
  const [hallazgos, setHallazgos] = useState<HallazgoResponse[]>(
    INITIAL_LOCAL_HALLAZGOS
  );

  // Formulario Avistamiento
  const [avEspecieId, setAvEspecieId] = useState(catalogo[0].id);
  const [avCantidad, setAvCantidad] = useState<number>(2);
  const [avDepto, setAvDepto] = useState('Vichada');
  const [avRegion, setAvRegion] = useState('Orinoquía');
  const [avLat, setAvLat] = useState('5.5412');
  const [avLon, setAvLon] = useState('-68.5194');

  // Formulario Hallazgo + Evidencias
  const [haEspecieId, setHaEspecieId] = useState(catalogo[0].id);
  // Expediciones de servicio-registro para asociar el hallazgo
  const [expediciones, setExpediciones] = useState<Expedicion[]>([]);
  const [haExpedicionId, setHaExpedicionId] = useState<string>('');
  const recargarExpediciones = async () => setExpediciones(await listarExpediciones());
  useEffect(() => {
    listarExpediciones().then((lista) => {
      setExpediciones(lista);
      // Se propone la expedición que lidera el usuario, si tiene una
      setHaExpedicionId(
        (actual) => actual || lista.find((ex) => ex.liderId === currentUser.id)?.id || ''
      );
    });
  }, [currentUser.id]);

  // Ficha de servicio-taxonomia: se elige entre las fichas activas de la especie seleccionada
  const [haFichaId, setHaFichaId] = useState<string>('');
  const fichasEspecie = (fichas ?? []).filter((f) => f.activa && f.especieId === haEspecieId);
  const primeraFicha = fichasEspecie[0]?.id ?? '';
  useEffect(() => {
    // Al cambiar de especie (o cargar las fichas) se propone su ficha vigente
    setHaFichaId(primeraFicha);
  }, [haEspecieId, primeraFicha]);
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

  // Modificación y Reclasificación
  const [targetHallazgoId, setTargetHallazgoId] = useState<string>(
    INITIAL_LOCAL_HALLAZGOS[0].id
  );
  const [busquedaHallazgo, setBusquedaHallazgo] = useState<string>('');
  const [modCampo, setModCampo] = useState<'descripcion' | 'departamento' | 'region'>(
    'descripcion'
  );
  const [modValor, setModValor] = useState<string>('');
  const [recNuevaEspecieId, setRecNuevaEspecieId] = useState<string>(
    catalogo[1].id
  );
  const [recMotivo, setRecMotivo] = useState<string>('');
  const [recAutorizador, setRecAutorizador] = useState<string>('');
  // Quién puede autorizar: cuentas con el permiso hallazgo:validar según servicio-autorizacion
  const [autorizadores, setAutorizadores] = useState<ResumenUsuario[]>([]);

  // Modal nativo <dialog> para confirmación de reclasificación
  const confirmDialogRef = useRef<HTMLDialogElement | null>(null);
  const [confirmOpen, setConfirmOpen] = useState<boolean>(false);

  const canWrite = canRegisterFieldData(currentUser);
  const canEdit = canEditFindings(currentUser);
  // Coordenadas exactas por especie: ubicacion:ver_exacta o acceso dado por la autoridad ambiental
  const puedeVerExacta = useAccesoUbicacion(currentUser);

  useEffect(() => {
    listarUsuariosConPermiso(currentUser.id, PERMISOS.VALIDAR_HALLAZGO).then((lista) => {
      setAutorizadores(lista);
      const propio = lista.find((u) => u.id === currentUser.id);
      setRecAutorizador((propio ?? lista[0])?.id ?? '');
    });
  }, [currentUser.id]);

  const nombreAutorizador = (id: string) =>
    autorizadores.find((u) => u.id === id)?.nombre ?? id;

  const syncWithBackend = async () => {
    const res = await listarAvistamientos(currentUser.id);
    if (res.online && res.data) {
      setAvistamientos((prev) => {
        const pendingItems = prev.filter((item) => item.pendingSync);
        return [...pendingItems, ...(res.data || [])];
      });
    }

    const resHallazgos = await listarHallazgos(currentUser.id);
    if (resHallazgos.online && resHallazgos.data) {
      const guardados = resHallazgos.data;
      setHallazgos((prev) => [...prev.filter((h) => h.pendingSync), ...guardados]);
    }
  };

  useEffect(() => {
    syncWithBackend();
  }, []);

  useEffect(() => {
    if (hallazgos.length > 0 && !hallazgos.some((h) => h.id === targetHallazgoId)) {
      setTargetHallazgoId(hallazgos[0].id);
    }
  }, [hallazgos, targetHallazgoId]);

  const handleDeptoSelect = (
    depto: string,
    setDeptoFn: (d: string) => void,
    setRegionFn: (r: string) => void
  ) => {
    setDeptoFn(depto);
    const match = DEPARTAMENTOS_REGIONES.find((d) => d.depto === depto);
    if (match) setRegionFn(match.region);
  };

  // Reintentar sincronización de un avistamiento guardado localmente
  const handleRetrySyncAvistamiento = async (item: AvistamientoResponse) => {
    if (!item.originalPayload) return;
    setLoading(true);
    setErrorFormulario(null);
    try {
      const saved = await registrarAvistamiento(item.originalPayload, currentUser.id);
      setAvistamientos((prev) =>
        prev.map((av) => (av.id === item.id ? { ...saved, pendingSync: false } : av))
      );
      notificar({
        tipo: 'ok',
        titulo: 'Avistamiento sincronizado',
        detalle: `Ya está en la base de datos con el código ${saved.id.slice(0, 8)}.`,
      });
    } catch (error) {
      if (error instanceof ErrorRechazado) {
        notificar(
          avisoDeError(error, {
            titulo: 'No se sincronizó el avistamiento',
            sinConexion: 'El cambio no se guardó',
            accion: 'registrar avistamientos',
          })
        );
        return;
      }
      notificar({
        tipo: 'aviso',
        titulo: 'Sigue sin conexión',
        detalle: 'El avistamiento continúa guardado en este equipo. Inténtalo más tarde.',
      });
    } finally {
      setLoading(false);
    }
  };

  // Registrar Avistamiento Diario
  const handleRegistrarAvistamiento = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorFormulario(null);

    if (!canWrite) {
      notificar({
        tipo: 'error',
        titulo: 'No tienes permiso',
        detalle: 'Tu rol no puede registrar avistamientos.',
      });
      return;
    }

    const latVal = avLat.trim() === '' ? null : Number(avLat);
    const lonVal = avLon.trim() === '' ? null : Number(avLon);

    setLoading(true);
    const payload: AvistamientoRequest = {
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
      const saved = await registrarAvistamiento(payload, currentUser.id);
      setAvistamientos((prev) => [{ ...saved, pendingSync: false }, ...prev]);
      notificar({
        tipo: 'ok',
        titulo: 'Avistamiento registrado',
        detalle: `${saved.cantidad} ${saved.cantidad === 1 ? 'individuo' : 'individuos'} de ${saved.especie}. Código ${saved.id.slice(0, 8)}.`,
      });
    } catch (error) {
      if (error instanceof ErrorRechazado) {
        notificar(
          avisoDeError(error, {
            titulo: 'No se registró el avistamiento',
            sinConexion: 'El cambio no se guardó',
            accion: 'registrar avistamientos',
          })
        );
        return;
      }
      const espObj = catalogo.find((s) => s.id === avEspecieId);
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
        pendingSync: true,
        originalPayload: payload,
      };
      setAvistamientos((prev) => [localSaved, ...prev]);
      notificar({
        tipo: 'aviso',
        titulo: 'Guardado solo en este equipo',
        detalle: 'No hay conexión con el servidor. Reintenta el envío desde la bitácora.',
      });
    } finally {
      setLoading(false);
    }
  };

  // Registrar Hallazgo + Evidencias
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
    setErrorFormulario(null);

    if (!canWrite) {
      notificar({
        tipo: 'error',
        titulo: 'No tienes permiso',
        detalle: 'Tu rol no puede registrar hallazgos.',
      });
      return;
    }

    const hasLat = haLat.trim() !== '';
    const hasLon = haLon.trim() !== '';
    if (hasLat !== hasLon) {
      setErrorFormulario({
        formulario: 'hallazgo',
        texto: 'Escribe la latitud y la longitud, o deja las dos en blanco.',
      });
      return;
    }

    for (const ev of haEvidencias) {
      if (ev.tipo === 'TEMPERATURA') {
        if (!ev.valor?.trim() || isNaN(Number(ev.valor))) {
          setErrorFormulario({
            formulario: 'hallazgo',
            texto: ev.valor?.trim()
              ? `La temperatura debe ser un número: "${ev.valor}" no lo es.`
              : 'Falta el valor de la temperatura.',
          });
          return;
        }
        if (!ev.unidad?.trim()) {
          setErrorFormulario({
            formulario: 'hallazgo',
            texto: 'Falta la unidad de la temperatura, por ejemplo °C.',
          });
          return;
        }
      } else if (ev.tipo === 'ADN' && !ev.valor?.trim()) {
        setErrorFormulario({
          formulario: 'hallazgo',
          texto: 'Falta el código de la muestra de ADN.',
        });
        return;
      } else if (ev.tipo === 'AUDIO' && !ev.urlArchivo?.trim()) {
        setErrorFormulario({
          formulario: 'hallazgo',
          texto: 'Falta el enlace del archivo de audio.',
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
      expedicionId: haExpedicionId || undefined,
      evidencias: haEvidencias,
    };

    const espMeta =
      catalogo.find((s) => s.id === haEspecieId) ||
      catalogo[0];

    try {
      const result = await registrarHallazgo(payload, currentUser.id);
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

      notificar({
        tipo: 'ok',
        titulo: 'Hallazgo registrado',
        detalle: `${espMeta.commonName} en ${created.departamento}. Código ${created.id.slice(0, 8)}.`,
      });
    } catch (error) {
      if (error instanceof ErrorRechazado) {
        notificar(
          avisoDeError(error, {
            titulo: 'No se registró el hallazgo',
            sinConexion: 'El cambio no se guardó',
            accion: 'registrar hallazgos',
          })
        );
        return;
      }
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
        pendingSync: true,
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

      notificar({
        tipo: 'aviso',
        titulo: 'Guardado solo en este equipo',
        detalle: 'No hay conexión con el servidor, así que el hallazgo aún no está en la base de datos.',
      });
    } finally {
      setLoading(false);
    }
  };


  // Buscar hallazgo por nombre: filtra la lista y, si el elegido ya no coincide, toma el primero que sí
  const hallazgosFiltrados = hallazgos.filter((h) =>
    coincideHallazgo(h, busquedaHallazgo, catalogo)
  );

  const handleBuscarHallazgo = (texto: string) => {
    setBusquedaHallazgo(texto);
    const coincidencias = hallazgos.filter((h) => coincideHallazgo(h, texto, catalogo));
    if (coincidencias.length > 0 && !coincidencias.some((h) => h.id === targetHallazgoId)) {
      setTargetHallazgoId(coincidencias[0].id);
    }
  };

  // Modificar Hallazgo
  const handleModificarHallazgo = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorFormulario(null);
    if (!canEdit) {
      notificar({
        tipo: 'error',
        titulo: 'No tienes permiso',
        detalle: 'Tu rol no puede modificar hallazgos.',
      });
      return;
    }
    const current = hallazgos.find((h) => h.id === targetHallazgoId);
    if (!current) return;

    const prevValue =
      modCampo === 'descripcion'
        ? current.descripcion
        : modCampo === 'departamento'
        ? current.departamento
        : current.region || '';

    if (prevValue === modValor.trim()) {
      setErrorFormulario({
        formulario: 'modificar',
        texto: 'El valor nuevo es igual al actual.',
      });
      return;
    }

    setLoading(true);
    try {
      const updated = await modificarHallazgo(targetHallazgoId, {
        campo: modCampo,
        valor: modValor.trim(),
        autor: currentUser.id,
      }, currentUser.id);
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
          authorizedBy: `${currentUser.name}`,
          reason: `Actualización de ${modCampo} en estación de campo`,
        },
        updated.id
      );
      setModValor('');
      notificar({
        tipo: 'ok',
        titulo: 'Hallazgo actualizado',
        detalle: `Quedó en la versión ${updated.versionActual} y el cambio está en el historial.`,
      });
    } catch (error) {
      if (error instanceof ErrorRechazado) {
        notificar(
          avisoDeError(error, {
            titulo: 'No se actualizó el hallazgo',
            sinConexion: 'El cambio no se guardó',
            accion: 'modificar hallazgos',
          })
        );
        return;
      }
      const nextVersion = current.versionActual + 1;
      const localUpdated: HallazgoResponse = {
        ...current,
        [modCampo]: modValor.trim(),
        versionActual: nextVersion,
        pendingSync: true,
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
          authorizedBy: `${currentUser.name}`,
          reason: `Actualización local de ${modCampo}`,
        },
        current.id
      );
      setModValor('');
      notificar({
        tipo: 'aviso',
        titulo: 'Cambio guardado solo en este equipo',
        detalle: 'No hay conexión con el servidor, así que el hallazgo no cambió en la base de datos.',
      });
    } finally {
      setLoading(false);
    }
  };

  // Paso 1 de Reclasificación: Validar y abrir diálogo de confirmación (<dialog>)
  const handlePrepareReclasificacion = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorFormulario(null);
    if (!canEdit) {
      notificar({
        tipo: 'error',
        titulo: 'No tienes permiso',
        detalle: 'Tu rol no puede reclasificar hallazgos.',
      });
      return;
    }
    const current = hallazgos.find((h) => h.id === targetHallazgoId);
    if (!current) return;

    if (current.especieId === recNuevaEspecieId) {
      setErrorFormulario({
        formulario: 'reclasificar',
        texto: 'El hallazgo ya está clasificado con esa especie.',
      });
      return;
    }

    if (!recMotivo.trim()) {
      setErrorFormulario({
        formulario: 'reclasificar',
        texto: 'Escribe la justificación científica de la reclasificación.',
      });
      return;
    }

    if (!recAutorizador) {
      setErrorFormulario({
        formulario: 'reclasificar',
        texto: 'No hay ninguna persona disponible para autorizar la reclasificación.',
      });
      return;
    }

    setConfirmOpen(true);
    if (confirmDialogRef.current && !confirmDialogRef.current.open) {
      confirmDialogRef.current.showModal();
    }
  };

  const closeConfirmDialog = () => {
    setConfirmOpen(false);
    if (confirmDialogRef.current && confirmDialogRef.current.open) {
      confirmDialogRef.current.close();
    }
  };

  // Paso 2 de Reclasificación: Confirmar y ejecutar
  const executeReclasificacionConfirmada = async () => {
    closeConfirmDialog();
    const current = hallazgos.find((h) => h.id === targetHallazgoId);
    const nuevaEsp = catalogo.find((s) => s.id === recNuevaEspecieId);
    if (!current || !nuevaEsp) return;

    setLoading(true);
    try {
      const updated = await reclasificarHallazgo(targetHallazgoId, {
        nuevaEspecieId: recNuevaEspecieId,
        motivo: recMotivo.trim(),
        autor: currentUser.id,
        autorizador: recAutorizador,
      }, currentUser.id);
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
          authorizedBy: `${currentUser.name} · Autorizó: ${nombreAutorizador(recAutorizador)}`,
          reason: recMotivo.trim(),
        },
        updated.id
      );
      setRecMotivo('');
      notificar({
        tipo: 'ok',
        titulo: 'Hallazgo reclasificado',
        detalle: `De ${current.especie} a ${updated.especie}. Quedó en la versión ${updated.versionActual}.`,
      });
    } catch (error) {
      if (error instanceof ErrorRechazado) {
        notificar(
          avisoDeError(error, {
            titulo: 'No se reclasificó el hallazgo',
            sinConexion: 'El cambio no se guardó',
            accion: 'reclasificar hallazgos',
          })
        );
        return;
      }
      const nextVersion = current.versionActual + 1;
      const localUpdated: HallazgoResponse = {
        ...current,
        especieId: nuevaEsp.id,
        especie: nuevaEsp.scientificName,
        versionActual: nextVersion,
        pendingSync: true,
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
          authorizedBy: `${currentUser.name} · Autorizó: ${nombreAutorizador(recAutorizador)}`,
          reason: recMotivo.trim(),
        },
        current.id
      );
      setRecMotivo('');
      notificar({
        tipo: 'aviso',
        titulo: 'Reclasificación guardada solo en este equipo',
        detalle: 'No hay conexión con el servidor, así que el hallazgo no cambió en la base de datos.',
      });
    } finally {
      setLoading(false);
    }
  };

  const selectedHallazgo = hallazgos.find((h) => h.id === targetHallazgoId);
  const targetNewSpecies = catalogo.find(
    (s) => s.id === recNuevaEspecieId
  );
  const visibleAvistamientos = avistamientos.slice(0, visibleCount);

  return (
    <div className="space-y-6">
      {/* Pestañas cortas en una sola fila con role="tablist" */}
      <div className="flex flex-wrap items-center gap-2 border-b border-line pb-3">
      <div
        role="tablist"
        aria-label="Módulos de estación de campo"
        className="flex flex-wrap gap-2"
      >
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'avistamientos'}
          onClick={() => {
            setActiveTab('avistamientos');
            setErrorFormulario(null);
          }}
          className={`min-h-11 rounded-md px-5 py-2.5 font-mono text-xs font-semibold transition-colors cursor-pointer ${
            activeTab === 'avistamientos'
              ? 'bg-forest-900 text-white'
              : 'bg-surface text-ink border border-line hover:border-forest-700'
          }`}
        >
          Avistamientos
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'hallazgos'}
          onClick={() => {
            setActiveTab('hallazgos');
            setErrorFormulario(null);
          }}
          className={`min-h-11 rounded-md px-5 py-2.5 font-mono text-xs font-semibold transition-colors cursor-pointer ${
            activeTab === 'hallazgos'
              ? 'bg-forest-900 text-white'
              : 'bg-surface text-ink border border-line hover:border-forest-700'
          }`}
        >
          Hallazgos
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'reclasificacion'}
          onClick={() => {
            setActiveTab('reclasificacion');
            setErrorFormulario(null);
          }}
          className={`min-h-11 rounded-md px-5 py-2.5 font-mono text-xs font-semibold transition-colors cursor-pointer ${
            activeTab === 'reclasificacion'
              ? 'bg-forest-900 text-white'
              : 'bg-surface text-ink border border-line hover:border-forest-700'
          }`}
        >
          Reclasificación
        </button>
        {canManageEquipment(currentUser) && (
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'gestion'}
            onClick={() => {
              setActiveTab('gestion');
              setErrorFormulario(null);
            }}
            className={`min-h-11 rounded-md px-5 py-2.5 font-mono text-xs font-semibold transition-colors cursor-pointer ${
              activeTab === 'gestion'
                ? 'bg-forest-900 text-white'
                : 'bg-surface text-ink border border-line hover:border-forest-700'
            }`}
          >
            Equipos y expediciones
          </button>
        )}
      </div>
        {/* Lleva a la sección "Inventario y fichas por especie" del panel */}
        <a
          href="#inventario"
          onClick={(e) => {
            e.preventDefault();
            document.getElementById('inventario')?.scrollIntoView({ behavior: 'smooth' });
          }}
          className="inline-flex min-h-11 items-center rounded-md border border-line bg-surface px-5 py-2.5 font-mono text-xs font-semibold text-ink transition-colors hover:border-forest-700"
        >
          Fichas Taxonómicas
        </a>
      </div>

      {activeTab === 'gestion' && (
        <GestionCampo
          currentUser={currentUser}
          expediciones={expediciones}
          onExpedicionesCambiaron={recargarExpediciones}
        />
      )}

      {/* PESTAÑA 1: AVISTAMIENTOS */}
      {activeTab === 'avistamientos' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Agregar una especie nueva al catálogo, al inicio de la sección (solo con hallazgo:crear) */}
          {canWrite && onEspecieCreada && (
            <div className="lg:col-span-12">
              <NuevaEspecie usuario={currentUser} onCreada={onEspecieCreada} />
            </div>
          )}

          {/* El formulario solo aparece si la cuenta puede registrar (hallazgo:crear); el invitado no lo ve */}
          {canWrite && (
          <Card as="section" className="lg:col-span-5 space-y-4">
            <div className="border-b border-line pb-3">
              <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                Bitácora diaria de expedición
              </span>
              <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
                Registrar avistamiento
              </h3>
            </div>

            <form onSubmit={handleRegistrarAvistamiento} className="space-y-4">
              <Field id="av-investigador" label="Investigador responsable">
                <input
                  id="av-investigador"
                  type="text"
                  readOnly
                  value={`${currentUser.name} (${currentUser.id})`}
                  className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink-muted"
                />
              </Field>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <Field
                  id="av-especie"
                  label="Especie observada"
                  className="sm:col-span-2"
                >
                  <select
                    id="av-especie"
                    value={avEspecieId}
                    onChange={(e) => setAvEspecieId(e.target.value)}
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  >
                    {catalogo.map((sp) => (
                      <option key={sp.id} value={sp.id}>
                        {sp.commonName} ({sp.scientificName}) [{sp.status}]
                      </option>
                    ))}
                  </select>
                </Field>

                <Field id="av-cantidad" label="Individuos">
                  <input
                    id="av-cantidad"
                    type="number"
                    min={1}
                    required
                    value={avCantidad}
                    onChange={(e) => setAvCantidad(Number(e.target.value))}
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                  />
                </Field>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <Field id="av-departamento" label="Departamento">
                  <select
                    id="av-departamento"
                    value={avDepto}
                    onChange={(e) =>
                      handleDeptoSelect(e.target.value, setAvDepto, setAvRegion)
                    }
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  >
                    {DEPARTAMENTOS_REGIONES.map((d) => (
                      <option key={d.depto} value={d.depto}>
                        {d.depto}
                      </option>
                    ))}
                  </select>
                </Field>

                <Field id="av-region" label="Región natural">
                  <input
                    id="av-region"
                    type="text"
                    value={avRegion}
                    onChange={(e) => setAvRegion(e.target.value)}
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  />
                </Field>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <Field id="av-latitud" label="Latitud (-90 a 90)">
                  <input
                    id="av-latitud"
                    type="number"
                    step="any"
                    min={-90}
                    max={90}
                    value={avLat}
                    onChange={(e) => setAvLat(e.target.value)}
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                  />
                </Field>
                <Field id="av-longitud" label="Longitud (-180 a 180)">
                  <input
                    id="av-longitud"
                    type="number"
                    step="any"
                    min={-180}
                    max={180}
                    value={avLon}
                    onChange={(e) => setAvLon(e.target.value)}
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink"
                  />
                </Field>
              </div>

              <Button
                type="submit"
                variant="primary"
                disabled={loading}
                className="w-full"
              >
                {loading ? 'Guardando avistamiento...' : 'Guardar avistamiento'}
              </Button>
            </form>
          </Card>
          )}

          {/* Bitácora con layout unificado y paginación incremental (a todo el ancho si no hay formulario) */}
          <Card as="section" className={`${canWrite ? 'lg:col-span-7' : 'lg:col-span-12'} space-y-4`}>
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line pb-3">
              <div>
                <span className="font-mono text-xs uppercase tracking-wider text-ink-muted">
                  Orden cronológico descendente
                </span>
                <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
                  Bitácora de avistamientos ({avistamientos.length})
                </h3>
              </div>
              <span className="font-mono text-xs text-ink-muted">
                Mostrando {visibleAvistamientos.length} de {avistamientos.length}
              </span>
            </div>

            <div className="space-y-3">
              {visibleAvistamientos.map((av) => {
                const spMeta = catalogo.find((s) => s.id === av.especieId);
                return (
                  <article
                    key={av.id}
                    className={`rounded-lg border p-4 space-y-3 ${
                      av.pendingSync
                        ? 'border-sun-500 bg-sun-500/5'
                        : 'border-line bg-paper'
                    }`}
                  >
                    {/* Fila superior unificada: Especie + Badge UICN + Individuos */}
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="font-serif italic font-semibold text-base text-ink">
                          {av.especie}
                        </span>
                        {spMeta && <ConservationBadge status={spMeta.status} />}
                      </div>
                      <span className="rounded bg-forest-900 px-2.5 py-0.5 font-mono text-xs font-semibold text-white">
                        {av.cantidad} {av.cantidad === 1 ? 'individuo' : 'individuos'}
                      </span>
                    </div>

                    {/* Fila intermedia unificada: Ubicación y coordenadas */}
                    <div className="text-xs text-ink-muted">
                      <span>
                        {av.departamento}
                        {av.region ? ` · Región ${av.region}` : ''}
                      </span>
                      {!puedeVerExacta(av.especieId) ? (
                        <span className="font-mono ml-2">· Coordenadas exactas ocultas</span>
                      ) : (
                        av.latitud !== null &&
                        av.longitud !== null && (
                          <span className="font-mono ml-2">
                            ({av.latitud.toFixed(4)}, {av.longitud.toFixed(4)})
                          </span>
                        )
                      )}
                    </div>

                    {/* Fila inferior unificada: Fecha, código y estado de sincronización */}
                    <div className="flex flex-wrap items-center justify-between gap-2 border-t border-line pt-2.5 font-mono text-xs">
                      <div className="text-ink-muted">
                        <time dateTime={av.fechaHora} className="text-ink">
                          {new Date(av.fechaHora).toLocaleString('es-CO')}
                        </time>
                        <span className="ml-2">· Código {av.id.slice(0, 8)}</span>
                      </div>

                      {av.pendingSync ? (
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="rounded bg-sun-500 px-2 py-0.5 text-[11px] font-semibold text-[#1B2A22]">
                            Guardado solo en este equipo, sin sincronizar
                          </span>
                          <button
                            type="button"
                            disabled={loading}
                            onClick={() => handleRetrySyncAvistamiento(av)}
                            className="min-h-11 rounded-md border border-forest-700 bg-surface px-3 py-1 text-xs font-semibold text-ink hover:bg-forest-900 hover:text-white transition-colors cursor-pointer"
                          >
                            Reintentar envío
                          </button>
                        </div>
                      ) : (
                        <span className="text-moss-500 font-semibold">
                          Sincronizado
                        </span>
                      )}
                    </div>
                  </article>
                );
              })}
            </div>

            {visibleCount < avistamientos.length && (
              <div className="pt-2 flex justify-center">
                <Button
                  variant="ghost"
                  onClick={() => setVisibleCount((prev) => prev + PAGE_SIZE)}
                  className="text-xs"
                >
                  Cargar más avistamientos ({avistamientos.length - visibleCount} restantes)
                </Button>
              </div>
            )}
          </Card>
        </div>
      )}

      {/* PESTAÑA 2: HALLAZGOS */}
      {activeTab === 'hallazgos' && (
        <div className="space-y-6">
        {/* El formulario solo aparece si la cuenta puede registrar (hallazgo:crear); el invitado solo ve la lista */}
        {canWrite && (
        <Card as="section" className="space-y-6">
          <div className="flex flex-wrap items-baseline justify-between gap-2 border-b border-line pb-4">
            <div>
              <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                Documentación científica de expedición
              </span>
              <h3 className="mt-1 font-serif text-2xl font-semibold text-ink">
                Registrar hallazgo biológico con evidencias
              </h3>
            </div>
            <span className="font-mono text-xs text-ink-muted">
              Responsable: {currentUser.name}
            </span>
          </div>

          <form onSubmit={handleRegistrarHallazgo} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Field id="ha-especie" label="Especie identificada">
                <select
                  id="ha-especie"
                  value={haEspecieId}
                  onChange={(e) => setHaEspecieId(e.target.value)}
                  className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                >
                  {catalogo.map((sp) => (
                    <option key={sp.id} value={sp.id}>
                      {sp.commonName} — {sp.scientificName} [{sp.status}]
                    </option>
                  ))}
                </select>
              </Field>

              <Field
                id="ha-ficha"
                label="Ficha taxonómica"
                hint={
                  fichasEspecie.length === 0
                    ? 'Esta especie aún no tiene ficha en servicio-taxonomía'
                    : undefined
                }
              >
                <select
                  id="ha-ficha"
                  value={haFichaId}
                  onChange={(e) => setHaFichaId(e.target.value)}
                  className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                >
                  {fichasEspecie.map((f) => (
                    <option key={f.id} value={f.id}>
                      Ficha v{f.version} · {f.atributos.length} atributo(s) · #{f.id.slice(0, 8)}
                    </option>
                  ))}
                  <option value="">Sin ficha asociada</option>
                </select>
              </Field>

              <Field
                id="ha-expedicion"
                label="Expedición"
                hint={expediciones.length === 0 ? 'No hay expediciones registradas' : undefined}
              >
                <select
                  id="ha-expedicion"
                  value={haExpedicionId}
                  onChange={(e) => setHaExpedicionId(e.target.value)}
                  className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                >
                  <option value="">Sin expedición</option>
                  {expediciones.map((ex) => (
                    <option key={ex.id} value={ex.id}>
                      {ex.nombre} · {ex.zona} · desde {ex.fechaInicio}
                      {ex.liderId === currentUser.id ? ' (tú la lideras)' : ''}
                    </option>
                  ))}
                </select>
              </Field>

              <div className="grid grid-cols-2 gap-2">
                <Field id="ha-departamento" label="Departamento">
                  <select
                    id="ha-departamento"
                    value={haDepto}
                    onChange={(e) =>
                      handleDeptoSelect(e.target.value, setHaDepto, setHaRegion)
                    }
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  >
                    {DEPARTAMENTOS_REGIONES.map((d) => (
                      <option key={d.depto} value={d.depto}>
                        {d.depto}
                      </option>
                    ))}
                  </select>
                </Field>
                <Field id="ha-region" label="Región">
                  <input
                    id="ha-region"
                    type="text"
                    value={haRegion}
                    onChange={(e) => setHaRegion(e.target.value)}
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  />
                </Field>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Field
                id="ha-descripcion"
                label="Descripción científica del hallazgo (máx. 1000 caracteres)"
                className="md:col-span-2"
              >
                <textarea
                  id="ha-descripcion"
                  rows={3}
                  maxLength={1000}
                  required
                  value={haDescripcion}
                  onChange={(e) => setHaDescripcion(e.target.value)}
                  placeholder="Describe el ejemplar, microhábitat, comportamiento observado y condiciones de muestreo..."
                  className="w-full rounded-md border border-line bg-paper px-3 py-2 text-sm text-ink"
                />
              </Field>

              <div className="rounded-lg border border-line bg-paper p-3.5 space-y-2">
                <p className="font-mono text-[11px] uppercase tracking-wider text-ink-muted">
                  Coordenadas GPS (ambas o ninguna)
                </p>
                <div className="grid grid-cols-2 gap-2">
                  <Field id="ha-latitud" label="Latitud">
                    <input
                      id="ha-latitud"
                      type="number"
                      step="any"
                      value={haLat}
                      onChange={(e) => setHaLat(e.target.value)}
                      className="min-h-11 w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                    />
                  </Field>
                  <Field id="ha-longitud" label="Longitud">
                    <input
                      id="ha-longitud"
                      type="number"
                      step="any"
                      value={haLon}
                      onChange={(e) => setHaLon(e.target.value)}
                      className="min-h-11 w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                    />
                  </Field>
                </div>
              </div>
            </div>

            {/* Evidencias asociadas */}
            <div className="space-y-3 border-t border-line pt-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <h4 className="font-serif text-lg font-semibold text-ink">
                    Evidencias recolectadas en campo
                  </h4>
                  <p className="text-xs text-ink-muted">
                    Adjunta mediciones térmicas, códigos de muestras genéticas o grabaciones acústicas:
                  </p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Button
                    variant="ghost"
                    onClick={() => addEvidencia('TEMPERATURA')}
                    className="text-xs"
                  >
                    + Temperatura
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => addEvidencia('ADN')}
                    className="text-xs"
                  >
                    + Muestra ADN
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => addEvidencia('AUDIO')}
                    className="text-xs"
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
                    <Field
                      id={`ev-tipo-${idx}`}
                      label="Tipo de evidencia"
                      className="md:col-span-2"
                    >
                      <select
                        id={`ev-tipo-${idx}`}
                        value={ev.tipo}
                        onChange={(e) =>
                          updateEvidencia(idx, {
                            tipo: e.target.value as TipoEvidencia,
                          })
                        }
                        className="min-h-11 w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                      >
                        <option value="TEMPERATURA">Temperatura</option>
                        <option value="ADN">Muestra ADN</option>
                        <option value="AUDIO">Audio</option>
                      </select>
                    </Field>

                    {ev.tipo === 'TEMPERATURA' && (
                      <>
                        <Field
                          id={`ev-valor-${idx}`}
                          label="Valor numérico"
                          className="md:col-span-3"
                        >
                          <input
                            id={`ev-valor-${idx}`}
                            type="text"
                            value={ev.valor || ''}
                            onChange={(e) =>
                              updateEvidencia(idx, { valor: e.target.value })
                            }
                            placeholder="28.5"
                            className="min-h-11 w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                          />
                        </Field>
                        <Field
                          id={`ev-unidad-${idx}`}
                          label="Unidad"
                          className="md:col-span-2"
                        >
                          <input
                            id={`ev-unidad-${idx}`}
                            type="text"
                            value={ev.unidad || ''}
                            onChange={(e) =>
                              updateEvidencia(idx, { unidad: e.target.value })
                            }
                            placeholder="C"
                            className="min-h-11 w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                          />
                        </Field>
                      </>
                    )}

                    {ev.tipo === 'ADN' && (
                      <Field
                        id={`ev-adn-${idx}`}
                        label="Código de muestra genética"
                        className="md:col-span-5"
                      >
                        <input
                          id={`ev-adn-${idx}`}
                          type="text"
                          value={ev.valor || ''}
                          onChange={(e) =>
                            updateEvidencia(idx, { valor: e.target.value })
                          }
                          placeholder="ADN-2026-901"
                          className="min-h-11 w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                        />
                      </Field>
                    )}

                    {ev.tipo === 'AUDIO' && (
                      <Field
                        id={`ev-audio-${idx}`}
                        label="Enlace del archivo acústico"
                        className="md:col-span-5"
                      >
                        <input
                          id={`ev-audio-${idx}`}
                          type="text"
                          value={ev.urlArchivo || ''}
                          onChange={(e) =>
                            updateEvidencia(idx, { urlArchivo: e.target.value })
                          }
                          placeholder="https://archivo.biohub.gov.co/canto.wav"
                          className="min-h-11 w-full rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-ink"
                        />
                      </Field>
                    )}

                    <Field
                      id={`ev-attr-${idx}`}
                      label="Parámetro medido"
                      className="md:col-span-4"
                    >
                      <input
                        id={`ev-attr-${idx}`}
                        type="text"
                        value={ev.atributoTaxonomico || ''}
                        onChange={(e) =>
                          updateEvidencia(idx, {
                            atributoTaxonomico: e.target.value,
                          })
                        }
                        placeholder="Temperatura del agua"
                        className="min-h-11 w-full rounded border border-line bg-surface px-2.5 py-1.5 text-xs text-ink"
                      />
                    </Field>

                    <div className="md:col-span-1 flex justify-end">
                      <button
                        type="button"
                        onClick={() => removeEvidencia(idx)}
                        className="min-h-11 min-w-11 rounded border border-line bg-surface px-2.5 py-1.5 font-mono text-xs text-iucn-cr hover:border-iucn-cr cursor-pointer"
                        aria-label={`Quitar evidencia ${idx + 1}`}
                      >
                        ✕
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <ErrorFormulario
              texto={errorFormulario?.formulario === 'hallazgo' ? errorFormulario.texto : null}
            />
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

        {/* Sumar evidencias a un hallazgo ya registrado (solo con hallazgo:editar) */}
        {canEdit && (
          <AgregarEvidencia
            currentUser={currentUser}
            hallazgos={hallazgos.filter((h) => !h.pendingSync)}
            onHallazgoActualizado={(actualizado) =>
              setHallazgos((prev) => prev.map((h) => (h.id === actualizado.id ? actualizado : h)))
            }
          />
        )}

        {/* Hallazgos guardados en la base de datos, con búsqueda por nombre */}
        <ListaHallazgos
          hallazgos={hallazgos}
          catalogo={catalogo}
          fichas={fichas ?? []}
          expediciones={expediciones}
          puedeVerExacta={puedeVerExacta}
          onModificar={
            canEdit
              ? (id) => {
                  setTargetHallazgoId(id);
                  setActiveTab('reclasificacion');
                }
              : undefined
          }
        />
        </div>
      )}

      {/* PESTAÑA 3: RECLASIFICACIÓN */}
      {activeTab === 'reclasificacion' && (
        <div className="space-y-6">
          <Card className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-end">
              <Field
                id="buscar-hallazgo-input"
                label="Buscar hallazgo por nombre"
                className="md:col-span-5"
              >
                <input
                  id="buscar-hallazgo-input"
                  type="search"
                  value={busquedaHallazgo}
                  onChange={(e) => handleBuscarHallazgo(e.target.value)}
                  placeholder="Especie, departamento, investigador..."
                  className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                />
              </Field>

              <Field
                id="target-hallazgo-select"
                label="Seleccionar hallazgo registrado"
                className="md:col-span-7"
              >
                <select
                  id="target-hallazgo-select"
                  value={targetHallazgoId}
                  onChange={(e) => setTargetHallazgoId(e.target.value)}
                  disabled={hallazgosFiltrados.length === 0}
                  className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                >
                  {hallazgosFiltrados.length === 0 && (
                    <option value={targetHallazgoId}>Ningún hallazgo coincide con la búsqueda</option>
                  )}
                  {hallazgosFiltrados.map((h) => (
                    <option key={h.id} value={h.id}>
                      {nombreComunHallazgo(h, catalogo)} ({h.especie}) — {h.departamento} · {h.investigadorId} · v{h.versionActual}
                    </option>
                  ))}
                </select>
              </Field>
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
                    {selectedHallazgo.departamento} ·{' '}
                    {selectedHallazgo.region || 'Sin región'}
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

          {/* Actualizar y reclasificar: solo con hallazgo:editar; el invitado solo consulta el hallazgo */}
          {canEdit && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card as="section" className="space-y-4">
              <div className="border-b border-line pb-3">
                <span className="font-mono text-xs uppercase tracking-wider text-moss-500 font-semibold">
                  Control de versiones inmutable
                </span>
                <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
                  Actualizar datos del hallazgo
                </h3>
              </div>

              <form onSubmit={handleModificarHallazgo} className="space-y-4">
                <Field id="mod-campo" label="Dato a actualizar">
                  <select
                    id="mod-campo"
                    value={modCampo}
                    onChange={(e) =>
                      setModCampo(
                        e.target.value as 'descripcion' | 'departamento' | 'region'
                      )
                    }
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  >
                    <option value="descripcion">Descripción científica</option>
                    <option value="departamento">Departamento</option>
                    <option value="region">Región natural</option>
                  </select>
                </Field>

                <Field id="mod-valor" label="Nuevo valor">
                  <textarea
                    id="mod-valor"
                    rows={3}
                    required
                    value={modValor}
                    onChange={(e) => setModValor(e.target.value)}
                    placeholder="Ingresa la información corregida o ampliada..."
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  />
                </Field>

                <Field id="mod-autor" label="Responsable de la modificación">
                  <input
                    id="mod-autor"
                    type="text"
                    readOnly
                    value={`${currentUser.name} (${currentUser.id})`}
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink-muted"
                  />
                </Field>

                <ErrorFormulario
                  texto={errorFormulario?.formulario === 'modificar' ? errorFormulario.texto : null}
                />
                <Button
                  type="submit"
                  variant="primary"
                  disabled={loading}
                  className="w-full"
                >
                  Guardar nueva versión
                </Button>
              </form>
            </Card>

            <Card as="section" className="space-y-4">
              <div className="border-b border-line pb-3">
                <span className="font-mono text-xs uppercase tracking-wider text-sun-500 font-semibold">
                  Auditoría taxonómica con doble firma
                </span>
                <h3 className="mt-1 font-serif text-xl font-semibold text-ink">
                  Reclasificar especie del hallazgo
                </h3>
              </div>

              <form onSubmit={handlePrepareReclasificacion} className="space-y-4">
                <Field id="rec-nueva-especie" label="Nueva especie determinada">
                  <select
                    id="rec-nueva-especie"
                    value={recNuevaEspecieId}
                    onChange={(e) => setRecNuevaEspecieId(e.target.value)}
                    className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  >
                    {catalogo.map((sp) => (
                      <option key={sp.id} value={sp.id}>
                        {sp.scientificName} ({sp.commonName}) [{sp.status}]
                      </option>
                    ))}
                  </select>
                </Field>

                <Field
                  id="rec-motivo"
                  label="Justificación científica (máx. 500 caracteres)"
                >
                  <textarea
                    id="rec-motivo"
                    rows={2}
                    maxLength={500}
                    required
                    value={recMotivo}
                    onChange={(e) => setRecMotivo(e.target.value)}
                    placeholder="Ej. Análisis genético de muestra de laboratorio confirma nueva clasificación..."
                    className="w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                  />
                </Field>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <Field id="rec-autor" label="Investigador solicitante">
                    <input
                      id="rec-autor"
                      type="text"
                      readOnly
                      value={currentUser.name}
                      className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 font-mono text-xs text-ink-muted"
                    />
                  </Field>
                  <Field id="rec-autorizador" label="Científico autorizador">
                    <select
                      id="rec-autorizador"
                      value={recAutorizador}
                      onChange={(e) => setRecAutorizador(e.target.value)}
                      className="min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink"
                    >
                      {autorizadores.length === 0 && (
                        <option value="">Sin autorizadores disponibles</option>
                      )}
                      {autorizadores.map((u) => (
                        <option key={u.id} value={u.id}>
                          {u.nombre}
                        </option>
                      ))}
                    </select>
                  </Field>
                </div>

                <ErrorFormulario
                  texto={errorFormulario?.formulario === 'reclasificar' ? errorFormulario.texto : null}
                />
                <Button
                  type="submit"
                  variant="secondary"
                  disabled={loading}
                  className="w-full"
                >
                  {canAuthorizeReclassification(currentUser)
                    ? 'Revisar y autorizar reclasificación'
                    : 'Revisar y registrar reclasificación'}
                </Button>
              </form>
            </Card>
          </div>
          )}
        </div>
      )}

      {/* Diálogo nativo <dialog> de confirmación antes de reclasificar */}
      <dialog
        ref={confirmDialogRef}
        onClose={() => setConfirmOpen(false)}
        aria-labelledby="confirm-reclass-title"
        className="m-auto w-full max-w-lg rounded-xl border border-line bg-surface p-6 text-ink backdrop:bg-ink/60"
      >
        {confirmOpen && selectedHallazgo && targetNewSpecies && (
          <div className="space-y-4">
            <div className="border-b border-line pb-3">
              <p className="font-mono text-xs uppercase tracking-wider text-sun-500 font-semibold">
                Confirmación de cambio taxonómico
              </p>
              <h3
                id="confirm-reclass-title"
                className="mt-1 font-serif text-xl font-semibold text-ink"
              >
                ¿Confirmas la reclasificación de este hallazgo?
              </h3>
            </div>

            <div className="space-y-3 rounded-lg border border-line bg-paper p-4 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <span className="text-ink-muted block">Especie anterior (se conserva):</span>
                  <span className="font-serif italic font-semibold text-base text-ink">
                    {selectedHallazgo.especie}
                  </span>
                </div>
                <div>
                  <span className="text-ink-muted block">Nueva especie vigente:</span>
                  <span className="font-serif italic font-semibold text-base text-moss-500">
                    {targetNewSpecies.scientificName}
                  </span>
                </div>
              </div>
              <div className="border-t border-line pt-2">
                <span className="text-ink-muted block">Motivo científico registrado:</span>
                <p className="mt-0.5 text-ink">{recMotivo}</p>
              </div>
              <div className="border-t border-line pt-2 font-mono text-[11px] text-ink-muted">
                Solicita: {currentUser.name} · Autoriza: {nombreAutorizador(recAutorizador)} · Nueva versión: v
                {selectedHallazgo.versionActual + 1}
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-end gap-2 pt-2">
              <Button variant="ghost" onClick={closeConfirmDialog}>
                Cancelar
              </Button>
              <Button variant="primary" onClick={executeReclasificacionConfirmada}>
                Confirmar reclasificación
              </Button>
            </div>
          </div>
        )}
      </dialog>
    </div>
  );
}
