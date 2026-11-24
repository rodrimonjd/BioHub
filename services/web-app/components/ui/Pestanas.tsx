'use client';

import React, { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { animate, motion, useMotionValue, useReducedMotion, useTransform } from 'motion/react';

export interface Pestana<T extends string> {
  id: T;
  label: string;
}

export interface PestanasProps<T extends string> {
  /** Nombre del grupo para lectores de pantalla. */
  etiqueta: string;
  pestanas: Pestana<T>[];
  activa: T;
  onCambio: (id: T) => void;
  /** Id del elemento role="tabpanel" que muestra el contenido. */
  idPanel: string;
}

const RAPIDO = { type: 'spring', stiffness: 520, damping: 38 } as const;
const LENTO = { type: 'spring', stiffness: 230, damping: 27 } as const;

/** Id de una pestaña, para que el panel la use en aria-labelledby. */
export function idDePestana(idPanel: string, id: string): string {
  return `${idPanel}-pestana-${id}`;
}

/**
 * Pestañas en un solo control: el fondo se estira hasta la opción elegida y se asienta.
 * Si no caben, la fila se desliza en lugar de partirse en varias.
 */
export function Pestanas<T extends string>({
  etiqueta,
  pestanas,
  activa,
  onCambio,
  idPanel,
}: PestanasProps<T>) {
  const riel = useRef<HTMLDivElement>(null);
  const botones = useRef(new Map<string, HTMLButtonElement>());
  const anterior = useRef<{ inicio: number; fin: number } | null>(null);
  const sinMovimiento = useReducedMotion();
  const [bordes, setBordes] = useState({ izquierda: false, derecha: false });

  const inicio = useMotionValue(0);
  const fin = useMotionValue(0);
  const alto = useMotionValue(1);
  const ancho = useTransform([inicio, fin], ([a, b]: number[]) => Math.max(0, b - a));

  const ubicar = useCallback(
    (conMovimiento: boolean) => {
      const boton = botones.current.get(activa);
      if (!boton) return;
      const destino = { inicio: boton.offsetLeft, fin: boton.offsetLeft + boton.offsetWidth };
      const previo = anterior.current;
      anterior.current = destino;
      if (!conMovimiento || !previo || sinMovimiento) {
        inicio.stop();
        fin.stop();
        inicio.set(destino.inicio);
        fin.set(destino.fin);
        return;
      }
      // El borde que va adelante llega primero y el otro lo alcanza: por eso se estira
      const haciaDerecha = destino.inicio > previo.inicio;
      animate(inicio, destino.inicio, haciaDerecha ? LENTO : RAPIDO);
      animate(fin, destino.fin, haciaDerecha ? RAPIDO : LENTO);
      animate(alto, [1, 0.84, 1], { duration: 0.42, ease: 'easeOut' });
    },
    [activa, alto, fin, inicio, sinMovimiento]
  );

  // El medidor usa siempre la versión vigente sin volver a crearse, para no cortar la animación
  const ubicarActual = useRef(ubicar);
  useLayoutEffect(() => {
    ubicarActual.current = ubicar;
  }, [ubicar]);

  useLayoutEffect(() => {
    const boton = botones.current.get(activa);
    const el = riel.current;
    const cambio = anterior.current !== null && boton?.offsetLeft !== anterior.current.inicio;
    ubicar(cambio);
    if (!boton || !el) return;
    const tope = el.scrollWidth - el.clientWidth;
    const destino = Math.max(0, Math.min(tope, boton.offsetLeft - (el.clientWidth - boton.offsetWidth) / 2));
    if (!cambio || sinMovimiento) {
      el.scrollLeft = destino;
      return;
    }
    const desliz = animate(el.scrollLeft, destino, {
      duration: 0.4,
      ease: 'easeOut',
      onUpdate: (valor) => {
        el.scrollLeft = valor;
      },
    });
    return () => desliz.stop();
  }, [activa, sinMovimiento, ubicar]);

  useEffect(() => {
    const el = riel.current;
    if (!el) return;
    const medir = () => {
      const izquierda = el.scrollLeft > 4;
      const derecha = el.scrollWidth - el.clientWidth - el.scrollLeft > 4;
      setBordes((prev) =>
        prev.izquierda === izquierda && prev.derecha === derecha ? prev : { izquierda, derecha }
      );
    };
    const tamano = new ResizeObserver(() => {
      ubicarActual.current(false);
      medir();
    });
    tamano.observe(el);
    botones.current.forEach((b) => tamano.observe(b));
    el.addEventListener('scroll', medir, { passive: true });
    return () => {
      tamano.disconnect();
      el.removeEventListener('scroll', medir);
    };
  }, [pestanas.length]);

  const alPulsar = (e: React.KeyboardEvent) => {
    const actual = pestanas.findIndex((p) => p.id === activa);
    const destino = {
      ArrowRight: (actual + 1) % pestanas.length,
      ArrowLeft: (actual - 1 + pestanas.length) % pestanas.length,
      Home: 0,
      End: pestanas.length - 1,
    }[e.key];
    if (destino === undefined) return;
    e.preventDefault();
    onCambio(pestanas[destino].id);
    botones.current.get(pestanas[destino].id)?.focus({ preventScroll: true });
  };

  return (
    <div className="relative max-w-full rounded-full border border-line bg-surface sm:w-fit">
      <div
        ref={riel}
        role="tablist"
        aria-label={etiqueta}
        onKeyDown={alPulsar}
        className="relative flex overflow-x-auto rounded-full p-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
      >
        <motion.span
          aria-hidden="true"
          className="absolute bottom-1 top-1 rounded-full bg-forest-700 shadow-[0_6px_16px_-6px_rgba(15,26,21,0.6)]"
          style={{ left: inicio, width: ancho, scaleY: alto }}
        />
        {pestanas.map((p) => {
          const esActiva = p.id === activa;
          return (
            <button
              key={p.id}
              ref={(el) => {
                if (el) botones.current.set(p.id, el);
                else botones.current.delete(p.id);
              }}
              type="button"
              role="tab"
              id={idDePestana(idPanel, p.id)}
              aria-selected={esActiva}
              aria-controls={idPanel}
              tabIndex={esActiva ? 0 : -1}
              onClick={() => onCambio(p.id)}
              className={`relative min-h-11 shrink-0 cursor-pointer whitespace-nowrap rounded-full px-5 text-[13px] font-semibold transition-colors duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-sun-500 ${
                esActiva ? 'text-white' : 'text-ink-muted hover:text-ink'
              }`}
            >
              {p.label}
            </button>
          );
        })}
      </div>

      <span
        aria-hidden="true"
        className={`pointer-events-none absolute inset-y-0 left-0 w-10 rounded-l-full bg-gradient-to-r from-surface to-transparent transition-opacity duration-200 ${
          bordes.izquierda ? 'opacity-100' : 'opacity-0'
        }`}
      />
      <span
        aria-hidden="true"
        className={`pointer-events-none absolute inset-y-0 right-0 w-10 rounded-r-full bg-gradient-to-l from-surface to-transparent transition-opacity duration-200 ${
          bordes.derecha ? 'opacity-100' : 'opacity-0'
        }`}
      />
    </div>
  );
}
