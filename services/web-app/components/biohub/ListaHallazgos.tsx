'use client';

import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { ConservationBadge } from '@/components/ui/ConservationBadge';
import { Field } from '@/components/ui/Field';
import {
  consultarHallazgos,
  ErrorConsulta,
  FiltroHallazgos,
  HallazgoPublico,
  RECONSULTAS_MS,
} from '@/lib/consulta-api';
import { coincideHallazgo, enRangoDeFechas, nombreComunHallazgo } from '@/lib/hallazgos';
import { BioHubUser, canEditFindings, canRegisterFieldData } from '@/lib/permissions';
import { Expedicion, HallazgoResponse, obtenerHallazgoPorId, SeedSpecies } from '@/lib/registro-api';
import { FichaTaxonomica } from '@/lib/taxonomia-api';

export interface ListaHallazgosProps {
  usuario: BioHubUser;
  /** Hallazgos guardados solo en este equipo: todavía no están en la consulta pública. */
  pendientes: HallazgoResponse[];
  catalogo: SeedSpecies[];
  /** Fichas de servicio-taxonomia, para mostrar la ficha asociada a cada hallazgo. */
  fichas: FichaTaxonomica[];
  /** Expediciones de servicio-registro, para mostrar en cuál se hizo cada hallazgo. */
  expediciones: Expedicion[];
  /** Si la cuenta ve las coordenadas exactas de esa especie (ubicacion:ver_exacta o acceso dado por la autoridad ambiental). */
  puedeVerExacta: (especieId: string) => boolean;
  /** Momento (ms) en que se guardó o modificó el último hallazgo; 0 si no ha habido cambios. */
  ultimoCambio?: number;
  /** Si la cuenta puede modificar hallazgos: lleva el hallazgo a la pestaña de reclasificación. */
  onModificar?: (id: string) => void;
  /** Si el hallazgo está en servicio-registro, que es donde se modifica. */
  sePuedeModificar?: (id: string) => boolean;
}

const POR_PAGINA = 6;
const TEXTO_MAXIMO = 100;

/** Espera después de escribir antes de consultar. */
const ESPERA_ESCRITURA_MS = 350;

const ETIQUETA_EVIDENCIA: Record<string, string> = {
  TEMPERATURA: 'Temperatura',
  ADN: 'Muestra ADN',
  AUDIO: 'Audio',
};

const CLASE_INPUT =
  'min-h-11 w-full rounded-md border border-line bg-paper px-3 py-2 text-xs text-ink focus:outline-none focus:ring-2 focus:ring-moss-500';

function fecha(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime())
    ? iso
    : d.toLocaleDateString('es-CO', { day: 'numeric', month: 'short', year: 'numeric' });
}

const dia = (aaaaMmDd: string) => fecha(`${aaaaMmDd}T12:00:00`);

function describirFiltro(f: FiltroHallazgos, especies: SeedSpecies[]): string {
  const partes: string[] = [];
  if (f.especie) {
    partes.push(`la especie ${especies.find((s) => s.scientificName === f.especie)?.commonName ?? f.especie}`);
  }
  if (f.texto) partes.push(`"${f.texto}"`);
  if (f.fechaDesde && f.fechaHasta) partes.push(`fecha entre el ${dia(f.fechaDesde)} y el ${dia(f.fechaHasta)}`);
  else if (f.fechaDesde) partes.push(`fecha desde el ${dia(f.fechaDesde)}`);
  else if (f.fechaHasta) partes.push(`fecha hasta el ${dia(f.fechaHasta)}`);
  return partes.join(', ');
}

/**
 * Consulta de hallazgos (HU-05): busca por especie, fecha y palabra clave en servicio-consulta-publica.
 * Al abrir un hallazgo, las cuentas que registran o editan ven además los datos internos de servicio-registro.
 */
export function ListaHallazgos({
  usuario,
  pendientes,
  catalogo,
  fichas,
  expediciones,
  puedeVerExacta,
  ultimoCambio = 0,
  onModificar,
  sePuedeModificar = () => true,
}: ListaHallazgosProps) {
  const [especie, setEspecie] = useState<string>('');
  const [texto, setTexto] = useState<string>('');
  const [textoBuscado, setTextoBuscado] = useState<string>('');
  const [desde, setDesde] = useState<string>('');
  const [hasta, setHasta] = useState<string>('');
  const [visibles, setVisibles] = useState<number>(POR_PAGINA);
  const [abierto, setAbierto] = useState<string | null>(null);

  const [resultado, setResultado] = useState<{ hallazgos: HallazgoPublico[]; total: number } | null>(null);
  const [consultando, setConsultando] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [intento, setIntento] = useState<number>(0);
  const [actualizando, setActualizando] = useState<boolean>(false);

  const verDatosInternos = canRegisterFieldData(usuario) || canEditFindings(usuario);
  const rangoInvertido = desde !== '' && hasta !== '' && desde > hasta;

  useEffect(() => {
    if (texto.trim() === textoBuscado) return;
    const t = setTimeout(() => {
      setTextoBuscado(texto.trim());
      setVisibles(POR_PAGINA);
    }, ESPERA_ESCRITURA_MS);
    return () => clearTimeout(t);
  }, [texto, textoBuscado]);

  useEffect(() => {
    if (rangoInvertido) {
      setConsultando(false);
      return;
    }
    const control = new AbortController();
    setConsultando(true);
    consultarHallazgos(
      { especie, texto: textoBuscado, fechaDesde: desde, fechaHasta: hasta },
      visibles,
      usuario.id,
      control.signal
    )
      .then((r) => {
        setResultado(r);
        setError(null);
        setConsultando(false);
      })
      .catch((e) => {
        if (control.signal.aborted) return;
        setError(e instanceof ErrorConsulta ? e.message : 'No se pudo consultar los hallazgos.');
        setConsultando(false);
      });
    return () => control.abort();
  }, [especie, textoBuscado, desde, hasta, visibles, rangoInvertido, usuario.id, intento]);

  // También al volver a esta pestaña: solo se programan las consultas que faltan desde el último cambio
  useEffect(() => {
    const pasado = Date.now() - ultimoCambio;
    const faltan = RECONSULTAS_MS.map((ms) => ms - pasado).filter((ms) => ms > 0);
    if (faltan.length === 0) {
      setActualizando(false);
      return;
    }
    setActualizando(true);
    const esperas = faltan.map((ms, i) =>
      setTimeout(() => {
        setIntento((n) => n + 1);
        if (i === faltan.length - 1) setActualizando(false);
      }, ms)
    );
    return () => esperas.forEach(clearTimeout);
  }, [ultimoCambio]);

  const filtro: FiltroHallazgos = { especie, texto: textoBuscado, fechaDesde: desde, fechaHasta: hasta };
  const hayFiltro = Object.values(filtro).some(Boolean);
  const locales = rangoInvertido
    ? []
    : pendientes.filter(
        (h) =>
          (!especie || h.especie === especie) &&
          coincideHallazgo(h, textoBuscado, catalogo) &&
          enRangoDeFechas(h, desde, hasta)
      );
  const idsLocales = new Set(locales.map((h) => h.id));
  const publicos = rangoInvertido
    ? []
    : (resultado?.hallazgos ?? []).filter((h) => !idsLocales.has(h.id));
  const total = rangoInvertido ? 0 : (resultado?.total ?? 0);
  const soloLocales = locales.filter((h) => !resultado?.hallazgos.some((p) => p.id === h.id)).length;
  const sinResultados = locales.length === 0 && publicos.length === 0;

  const cambiarFiltro = (aplicar: () => void) => {
    aplicar();
    setVisibles(POR_PAGINA);
  };

  const limpiar = () => {
    setEspecie('');
    setTexto('');
    setTextoBuscado('');
    setDesde('');
    setHasta('');
    setVisibles(POR_PAGINA);
  };

  return (
    <Card as="section" aria-labelledby="lista-hallazgos-heading" className="space-y-4">
      <div className="border-b border-line pb-4">
        <span className="font-mono text-xs uppercase tracking-wider text-ink-muted">Consulta pública</span>
        <h3 id="lista-hallazgos-heading" className="mt-1 font-serif text-2xl font-semibold text-ink">
          Consultar hallazgos{resultado ? ` (${total + soloLocales})` : ''}
        </h3>
      </div>

      <div role="search" aria-label="Buscar hallazgos" className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-12">
        <Field id="consulta-especie" label="Especie" className="lg:col-span-3">
          <select
            id="consulta-especie"
            value={especie}
            onChange={(e) => cambiarFiltro(() => setEspecie(e.target.value))}
            className={CLASE_INPUT}
          >
            <option value="">Todas las especies</option>
            {catalogo.map((s) => (
              <option key={s.id} value={s.scientificName}>
                {s.commonName} ({s.scientificName})
              </option>
            ))}
          </select>
        </Field>
        <Field id="consulta-texto" label="Palabra clave" className="lg:col-span-5">
          <input
            id="consulta-texto"
            type="search"
            value={texto}
            maxLength={TEXTO_MAXIMO}
            onChange={(e) => setTexto(e.target.value)}
            placeholder="Especie, descripción o departamento"
            className={CLASE_INPUT}
          />
        </Field>
        <Field id="consulta-desde" label="Desde" className="lg:col-span-2">
          <input
            id="consulta-desde"
            type="date"
            value={desde}
            max={hasta || undefined}
            onChange={(e) => cambiarFiltro(() => setDesde(e.target.value))}
            className={`${CLASE_INPUT} font-mono`}
          />
        </Field>
        <Field id="consulta-hasta" label="Hasta" className="lg:col-span-2">
          <input
            id="consulta-hasta"
            type="date"
            value={hasta}
            min={desde || undefined}
            onChange={(e) => cambiarFiltro(() => setHasta(e.target.value))}
            className={`${CLASE_INPUT} font-mono`}
          />
        </Field>
      </div>

      <div className="flex min-h-11 flex-wrap items-center justify-between gap-3">
        <p role="status" className="text-xs text-ink-muted">
          {consultando
            ? 'Consultando…'
            : actualizando
              ? 'Los cambios recientes pueden tardar unos segundos en aparecer.'
              : ''}
        </p>
        {(hayFiltro || texto) && (
          <Button variant="ghost" className="text-xs" onClick={limpiar}>
            Limpiar búsqueda
          </Button>
        )}
      </div>

      {error && !rangoInvertido && (
        <div
          role="alert"
          className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-iucn-cr/50 bg-iucn-cr/10 p-4"
        >
          <p className="text-sm text-ink">
            {error}
            {resultado ? ' Se muestra el último resultado.' : ''}
          </p>
          <Button variant="ghost" className="text-xs" onClick={() => setIntento((n) => n + 1)}>
            Reintentar
          </Button>
        </div>
      )}

      {sinResultados ? (
        rangoInvertido ? (
          <p role="alert" className="py-6 text-center text-sm text-ink-muted">
            La fecha &quot;Desde&quot; no puede ser posterior a la fecha &quot;Hasta&quot;.
          </p>
        ) : (
          resultado &&
          !consultando &&
          !error && (
            <p className="py-6 text-center text-sm text-ink-muted">
              {hayFiltro
                ? `No se encontraron hallazgos con ${describirFiltro(filtro, catalogo)}.`
                : 'Todavía no hay hallazgos registrados.'}
            </p>
          )
        )
      ) : (
        <ul className={`space-y-3 transition-opacity duration-150 ${consultando ? 'opacity-60' : ''}`}>
          {locales.map((h) => (
            <li key={h.id} className="space-y-2 rounded-lg border border-sun-500 bg-sun-500/5 p-4">
              <p className="font-serif text-base font-semibold text-ink">{nombreComunHallazgo(h, catalogo)}</p>
              <p className="font-serif text-sm italic text-ink-muted">{h.especie}</p>
              <p className="font-mono text-xs text-ink-muted">
                {h.departamento} · {fecha(h.fecha)} · #{h.id.slice(0, 8)} · pendiente de sincronizar
              </p>
              <p className="text-sm text-ink">{h.descripcion}</p>
            </li>
          ))}
          {publicos.map((h) => {
            const expandido = abierto === h.id;
            return (
              <li key={h.id} className="space-y-3 rounded-lg border border-line bg-paper p-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="font-serif text-base font-semibold text-ink">{h.nombreComun ?? h.especie}</p>
                    <p className="font-serif text-sm italic text-ink-muted">{h.especie}</p>
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    {h.categoriaConservacion && <ConservationBadge status={h.categoriaConservacion} />}
                    <span className="font-mono text-xs text-ink-muted">v{h.version}</span>
                  </div>
                </div>

                <p className="font-mono text-xs text-ink-muted">
                  {h.departamento}
                  {h.region ? ` · ${h.region}` : ''} · {fecha(h.fecha)} · #{h.id.slice(0, 8)}
                </p>

                {expandido && (
                  <div className="space-y-3 border-t border-line pt-3 text-xs">
                    <p className="text-sm text-ink">{h.descripcion}</p>
                    <p className="font-mono text-ink-muted">
                      {puedeVerExacta(h.especieId ?? '') && h.nivelUbicacion === 'EXACTA'
                        ? h.latitud !== null && h.longitud !== null
                          ? `Coordenadas: ${h.latitud}, ${h.longitud}`
                          : 'Sin coordenadas registradas'
                        : 'Coordenadas exactas ocultas (requiere acceso a la ubicación de esta especie)'}
                    </p>
                    {verDatosInternos && (
                      <DatosInternos
                        key={`${h.id}-${h.version}`}
                        id={h.id}
                        usuarioId={usuario.id}
                        fichas={fichas}
                        expediciones={expediciones}
                      />
                    )}
                  </div>
                )}

                <div className="flex flex-wrap gap-2">
                  <Button
                    variant="ghost"
                    className="text-xs"
                    aria-expanded={expandido}
                    onClick={() => setAbierto(expandido ? null : h.id)}
                  >
                    {expandido ? 'Ocultar detalle' : 'Ver detalle'}
                  </Button>
                  {onModificar && sePuedeModificar(h.id) && (
                    <Button variant="ghost" className="text-xs" onClick={() => onModificar(h.id)}>
                      Modificar o reclasificar
                    </Button>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}

      {!rangoInvertido && total > visibles && (
        <div className="flex justify-center">
          <Button
            variant="ghost"
            className="text-xs"
            disabled={consultando}
            onClick={() => setVisibles((n) => n + POR_PAGINA)}
          >
            Ver más hallazgos ({total - visibles} restantes)
          </Button>
        </div>
      )}
    </Card>
  );
}

interface DatosInternosProps {
  id: string;
  usuarioId: string;
  fichas: FichaTaxonomica[];
  expediciones: Expedicion[];
}

/** Investigador, expedición, ficha y evidencias: no son públicos, así que se piden a servicio-registro al abrir. */
function DatosInternos({ id, usuarioId, fichas, expediciones }: DatosInternosProps) {
  const [hallazgo, setHallazgo] = useState<HallazgoResponse | null>(null);
  const [fallo, setFallo] = useState<boolean>(false);

  useEffect(() => {
    let vigente = true;
    obtenerHallazgoPorId(id, usuarioId)
      .then((h) => vigente && setHallazgo(h))
      .catch(() => vigente && setFallo(true));
    return () => {
      vigente = false;
    };
  }, [id, usuarioId]);

  if (fallo) {
    return <p className="text-ink-muted">No se pudieron cargar las evidencias y los datos internos de este hallazgo.</p>;
  }
  if (!hallazgo) return <p className="text-ink-muted">Cargando evidencias…</p>;

  const expedicion = expediciones.find((e) => e.id === hallazgo.expedicionId);
  const ficha = fichas.find((f) => f.id === hallazgo.fichaId);
  return (
    <div className="space-y-3 font-mono text-ink-muted">
      <p>Investigador: {hallazgo.investigadorId}</p>
      <p>
        {!hallazgo.expedicionId
          ? 'Sin expedición asociada'
          : expedicion
            ? `Expedición: ${expedicion.nombre} · ${expedicion.zona}`
            : `Expedición #${hallazgo.expedicionId.slice(0, 8)}`}
      </p>
      <p>
        {!hallazgo.fichaId
          ? 'Sin ficha taxonómica asociada'
          : ficha
            ? `Ficha taxonómica v${ficha.version} · ${
                ficha.atributos
                  .filter((a) => a.atributo && a.valor)
                  .map((a) => `${a.atributo!.nombre}: ${a.valor}`)
                  .join(' · ') || 'sin atributos'
              }`
            : `Ficha taxonómica #${hallazgo.fichaId.slice(0, 8)}`}
      </p>
      {hallazgo.evidencias.length === 0 ? (
        <p>Sin evidencias registradas</p>
      ) : (
        <ul className="space-y-1">
          {hallazgo.evidencias.map((ev) => (
            <li key={ev.id} className="text-ink">
              {ETIQUETA_EVIDENCIA[ev.tipo] ?? ev.tipo}
              {ev.atributoTaxonomico ? ` · ${ev.atributoTaxonomico}` : ''}
              {ev.valor ? `: ${ev.valor}${ev.unidad ? ` ${ev.unidad}` : ''}` : ''}
              {ev.urlArchivo ? `: ${ev.urlArchivo}` : ''}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
