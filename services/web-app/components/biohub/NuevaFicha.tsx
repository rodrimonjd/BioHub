'use client';

import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { ErrorFormulario } from '@/components/ui/ErrorFormulario';
import { Field } from '@/components/ui/Field';
import { useNotificar } from '@/components/ui/Notificaciones';
import { avisoDeError } from '@/lib/avisos';
import { CLASE_INPUT, Encabezado } from './campo-ui';
import {
  AtributoTaxonomico,
  crearAtributo,
  crearFicha,
  EspecieRegistro,
  FichaTaxonomica,
  listarAtributos,
  TipoDato,
} from '@/lib/taxonomia-api';

export interface NuevaFichaProps {
  especies: EspecieRegistro[];
  fichas: FichaTaxonomica[];
  onCreada: () => Promise<void> | void;
}

interface FilaAtributo {
  clave: number;
  atributoId: string;
  nombre: string;
  tipoDato: TipoDato;
  unidad: string;
  opciones: string;
  valor: string;
}

const NUEVO = 'nuevo';

const TIPOS: { valor: TipoDato; etiqueta: string }[] = [
  { valor: 'TEXTO', etiqueta: 'Texto' },
  { valor: 'NUMERICO', etiqueta: 'Numérico' },
  { valor: 'LISTA', etiqueta: 'Lista de opciones' },
];

function filaVacia(clave: number): FilaAtributo {
  return { clave, atributoId: NUEVO, nombre: '', tipoDato: 'TEXTO', unidad: '', opciones: '', valor: '' };
}

function separarOpciones(texto: string): string[] {
  return texto
    .split(',')
    .map((opcion) => opcion.trim())
    .filter((opcion) => opcion !== '');
}

function nombreEspecie(especie: EspecieRegistro): string {
  return especie.nombreComun
    ? `${especie.nombreComun} (${especie.nombreCientifico})`
    : especie.nombreCientifico;
}

export function NuevaFicha({ especies, fichas, onCreada }: NuevaFichaProps) {
  const [catalogo, setCatalogo] = useState<AtributoTaxonomico[]>([]);
  const [especieId, setEspecieId] = useState<string>('');
  const [filas, setFilas] = useState<FilaAtributo[]>([filaVacia(1)]);
  const [siguienteClave, setSiguienteClave] = useState<number>(2);
  const [guardando, setGuardando] = useState<boolean>(false);
  const notificar = useNotificar();
  const [problema, setProblema] = useState<string | null>(null);

  const especiesSinFicha = especies.filter((e) => !fichas.some((f) => f.activa && f.especieId === e.id));
  const especie = especiesSinFicha.find((e) => e.id === especieId) ?? especiesSinFicha[0];

  useEffect(() => {
    listarAtributos().then(setCatalogo);
  }, [fichas]);

  const definicion = (fila: FilaAtributo) => {
    const existente = catalogo.find((a) => a.id === fila.atributoId);
    return existente
      ? {
          nombre: existente.nombre,
          tipoDato: existente.tipoDato,
          unidad: existente.unidad ?? '',
          opciones: existente.opciones ?? [],
        }
      : {
          nombre: fila.nombre.trim(),
          tipoDato: fila.tipoDato,
          unidad: fila.unidad.trim(),
          opciones: separarOpciones(fila.opciones),
        };
  };

  const cambiarFila = (clave: number, cambios: Partial<FilaAtributo>) => {
    setFilas((prev) => prev.map((f) => (f.clave === clave ? { ...f, ...cambios } : f)));
  };

  const agregarFila = () => {
    setFilas((prev) => [...prev, filaVacia(siguienteClave)]);
    setSiguienteClave((clave) => clave + 1);
  };

  const buscarProblema = (): string | null => {
    const nombres: string[] = [];
    for (const fila of filas) {
      const { nombre, tipoDato, opciones } = definicion(fila);
      if (!nombre) return 'Todos los atributos necesitan un nombre.';
      if (nombres.includes(nombre.toLowerCase())) return `El atributo "${nombre}" está repetido.`;
      nombres.push(nombre.toLowerCase());
      if (fila.atributoId === NUEVO && catalogo.some((a) => a.nombre.toLowerCase() === nombre.toLowerCase())) {
        return `El atributo "${nombre}" ya existe en el catálogo: elígelo de la lista.`;
      }
      if (tipoDato === 'LISTA' && opciones.length === 0) {
        return `El atributo "${nombre}" necesita al menos una opción.`;
      }
      if (!fila.valor.trim()) return `Falta el valor de "${nombre}".`;
      if (tipoDato === 'NUMERICO' && !Number.isFinite(Number(fila.valor))) {
        return `El valor de "${nombre}" debe ser un número.`;
      }
      if (tipoDato === 'LISTA' && !opciones.includes(fila.valor)) {
        return `Elige una de las opciones de "${nombre}".`;
      }
    }
    return null;
  };

  const handleGuardar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!especie) return;
    const encontrado = buscarProblema();
    setProblema(encontrado);
    if (encontrado) return;
    setGuardando(true);
    try {
      for (const fila of filas) {
        if (fila.atributoId !== NUEVO) continue;
        const { nombre, tipoDato, unidad, opciones } = definicion(fila);
        const creado = await crearAtributo({ nombre, tipoDato, unidad, opciones });
        setCatalogo((prev) => [...prev, creado]);
        cambiarFila(fila.clave, { atributoId: creado.id });
      }
      const atributos = filas.map((fila) => ({ nombre: definicion(fila).nombre, valor: fila.valor.trim() }));
      const ficha = await crearFicha(especie.id, atributos);
      notificar({
        tipo: 'ok',
        titulo: 'Ficha creada',
        detalle: `${especie.nombreComun ?? especie.nombreCientifico}, con ${ficha.atributos.length} ${
          ficha.atributos.length === 1 ? 'atributo' : 'atributos'
        }.`,
      });
      setFilas([filaVacia(siguienteClave)]);
      setSiguienteClave((clave) => clave + 1);
      setEspecieId('');
      await onCreada();
    } catch (error) {
      notificar(
        avisoDeError(error, {
          titulo: 'No se creó la ficha',
          sinConexion: 'La ficha no se guardó',
          accion: 'crear fichas taxonómicas',
        })
      );
    } finally {
      setGuardando(false);
    }
  };

  return (
    <Card className="bg-surface space-y-4">
      <Encabezado
        titulo="Crear ficha taxonómica"
        descripcion="Elige la especie y llena sus atributos. Puedes usar los del catálogo o definir nuevos, cada uno con su tipo de dato."
      />

      {!especie ? (
        <p className="text-xs text-ink-muted">
          Todas las especies registradas ya tienen una ficha activa. Agrega una especie nueva o incorpora atributos a
          una ficha existente.
        </p>
      ) : (
        <form onSubmit={handleGuardar} className="space-y-4">
          <Field id="ficha-especie" label="Especie (solo las que no tienen ficha)">
            <select
              id="ficha-especie"
              value={especie.id}
              onChange={(e) => setEspecieId(e.target.value)}
              className={CLASE_INPUT}
            >
              {especiesSinFicha.map((e) => (
                <option key={e.id} value={e.id}>
                  {nombreEspecie(e)} [{e.estadoConservacion}]
                </option>
              ))}
            </select>
          </Field>

          {filas.map((fila, indice) => {
            const { tipoDato, unidad, opciones } = definicion(fila);
            const esNuevo = fila.atributoId === NUEVO;
            const id = `ficha-atributo-${fila.clave}`;
            return (
              <fieldset key={fila.clave} className="space-y-3 rounded-md border border-line p-4">
                <legend className="px-1 font-mono text-xs font-semibold text-moss-500">Atributo {indice + 1}</legend>
                <div className="grid grid-cols-1 md:grid-cols-12 gap-3 items-end">
                  <Field id={id} label="Atributo" className="md:col-span-4">
                    <select
                      id={id}
                      value={fila.atributoId}
                      onChange={(e) => cambiarFila(fila.clave, { atributoId: e.target.value, valor: '' })}
                      className={CLASE_INPUT}
                    >
                      <option value={NUEVO}>+ Definir uno nuevo</option>
                      {catalogo.map((a) => (
                        <option key={a.id} value={a.id}>
                          {a.nombre}
                          {a.unidad ? ` (${a.unidad})` : ''}
                        </option>
                      ))}
                    </select>
                  </Field>

                  {esNuevo && (
                    <>
                      <Field id={`${id}-nombre`} label="Nombre" className="md:col-span-5">
                        <input
                          id={`${id}-nombre`}
                          required
                          maxLength={80}
                          value={fila.nombre}
                          onChange={(e) => cambiarFila(fila.clave, { nombre: e.target.value })}
                          placeholder="Ej. Longitud total"
                          className={CLASE_INPUT}
                        />
                      </Field>
                      <Field id={`${id}-tipo`} label="Tipo de dato" className="md:col-span-3">
                        <select
                          id={`${id}-tipo`}
                          value={fila.tipoDato}
                          onChange={(e) =>
                            cambiarFila(fila.clave, { tipoDato: e.target.value as TipoDato, valor: '' })
                          }
                          className={CLASE_INPUT}
                        >
                          {TIPOS.map((t) => (
                            <option key={t.valor} value={t.valor}>
                              {t.etiqueta}
                            </option>
                          ))}
                        </select>
                      </Field>
                    </>
                  )}

                  {esNuevo && tipoDato === 'NUMERICO' && (
                    <Field id={`${id}-unidad`} label="Unidad (opcional)" className="md:col-span-4">
                      <input
                        id={`${id}-unidad`}
                        value={fila.unidad}
                        onChange={(e) => cambiarFila(fila.clave, { unidad: e.target.value })}
                        placeholder="Ej. cm"
                        className={CLASE_INPUT}
                      />
                    </Field>
                  )}
                  {esNuevo && tipoDato === 'LISTA' && (
                    <Field id={`${id}-opciones`} label="Opciones separadas por comas" className="md:col-span-7">
                      <input
                        id={`${id}-opciones`}
                        required
                        value={fila.opciones}
                        onChange={(e) => cambiarFila(fila.clave, { opciones: e.target.value, valor: '' })}
                        placeholder="Ej. Diurno, Nocturno, Crepuscular"
                        className={CLASE_INPUT}
                      />
                    </Field>
                  )}

                  <Field
                    id={`${id}-valor`}
                    label={unidad && tipoDato === 'NUMERICO' ? `Valor (${unidad})` : 'Valor'}
                    className="md:col-span-5"
                  >
                    {tipoDato === 'LISTA' ? (
                      <select
                        id={`${id}-valor`}
                        required
                        value={fila.valor}
                        onChange={(e) => cambiarFila(fila.clave, { valor: e.target.value })}
                        className={CLASE_INPUT}
                      >
                        <option value="">Elegir...</option>
                        {opciones.map((opcion, posicion) => (
                          <option key={`${opcion}-${posicion}`} value={opcion}>
                            {opcion}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <input
                        id={`${id}-valor`}
                        type={tipoDato === 'NUMERICO' ? 'number' : 'text'}
                        step={tipoDato === 'NUMERICO' ? 'any' : undefined}
                        maxLength={tipoDato === 'TEXTO' ? 500 : undefined}
                        required
                        value={fila.valor}
                        onChange={(e) => cambiarFila(fila.clave, { valor: e.target.value })}
                        className={CLASE_INPUT}
                      />
                    )}
                  </Field>

                  {filas.length > 1 && (
                    <div className="md:col-span-3">
                      <Button
                        variant="ghost"
                        className="w-full text-xs"
                        onClick={() => setFilas((prev) => prev.filter((f) => f.clave !== fila.clave))}
                      >
                        Quitar
                      </Button>
                    </div>
                  )}
                </div>
              </fieldset>
            );
          })}

          <ErrorFormulario texto={problema} />

          <div className="flex flex-wrap gap-3">
            <Button variant="ghost" className="text-xs" onClick={agregarFila} disabled={guardando}>
              + Agregar otro atributo
            </Button>
            <Button type="submit" className="text-xs" disabled={guardando}>
              {guardando ? 'Guardando...' : 'Guardar ficha'}
            </Button>
          </div>
        </form>
      )}
    </Card>
  );
}
