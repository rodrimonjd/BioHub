'use client';

import React, { useEffect, useId, useLayoutEffect, useRef, useState } from 'react';
import { AnimatePresence, motion, useReducedMotion } from 'motion/react';

export interface OpcionSelector {
  valor: string;
  texto: string;
}

export interface SelectorDeslizanteProps {
  /** Nombre del control para lectores de pantalla. */
  etiqueta: string;
  /** Texto del botón: lo que está elegido hoy. */
  texto: string;
  opciones: OpcionSelector[];
  /** Opción marcada en el menú, si ya se eligió alguna. */
  valor: string | null;
  onElegir: (valor: string) => void;
  deshabilitado?: boolean;
  ref?: React.Ref<HTMLButtonElement>;
}

const ALTO_OPCION = 44;
const RELLENO_MENU = 4;
const ANCHO_MENU = 236;
const MARGEN = 12;

/** Espacio que tapa el navbar fijo cuando el menú se abre hacia arriba. */
const ALTO_NAVBAR = 80;

/**
 * Etiqueta que abre un menú desde su esquina; el resaltado se desliza entre las opciones.
 * El foco se queda en el botón y las flechas mueven la opción (patrón combobox de solo selección).
 */
export function SelectorDeslizante({
  etiqueta,
  texto,
  opciones,
  valor,
  onElegir,
  deshabilitado = false,
  ref,
}: SelectorDeslizanteProps) {
  const [abierto, setAbierto] = useState(false);
  const [resaltada, setResaltada] = useState(0);
  const [lugar, setLugar] = useState({ arriba: false, derecha: false });
  const caja = useRef<HTMLDivElement>(null);
  const idMenu = useId();
  const sinMovimiento = useReducedMotion();

  const abrir = () => {
    if (deshabilitado || opciones.length === 0) return;
    const elegida = opciones.findIndex((o) => o.valor === valor);
    setResaltada(Math.max(0, elegida));
    setAbierto(true);
  };

  const elegir = (indice: number) => {
    const opcion = opciones[indice];
    setAbierto(false);
    if (opcion) onElegir(opcion.valor);
  };

  // El menú se abre hacia donde haya espacio en la pantalla
  useLayoutEffect(() => {
    if (!abierto || !caja.current) return;
    const r = caja.current.getBoundingClientRect();
    const alto = opciones.length * ALTO_OPCION + RELLENO_MENU * 2 + MARGEN;
    setLugar({
      arriba: r.bottom + alto > window.innerHeight && r.top - alto > ALTO_NAVBAR,
      derecha: r.left + ANCHO_MENU + MARGEN > document.documentElement.clientWidth,
    });
  }, [abierto, opciones.length]);

  useEffect(() => {
    if (!abierto) return;
    const fuera = (e: PointerEvent) => {
      if (!caja.current?.contains(e.target as Node)) setAbierto(false);
    };
    document.addEventListener('pointerdown', fuera);
    return () => document.removeEventListener('pointerdown', fuera);
  }, [abierto]);

  useEffect(() => {
    if (deshabilitado) setAbierto(false);
  }, [deshabilitado]);

  const alPulsar = (e: React.KeyboardEvent) => {
    if (!abierto) {
      if (['ArrowDown', 'ArrowUp', 'Enter', ' '].includes(e.key)) {
        e.preventDefault();
        abrir();
      }
      return;
    }
    const total = opciones.length;
    if (e.key === 'Escape') {
      e.preventDefault();
      setAbierto(false);
    } else if (e.key === 'Tab') {
      setAbierto(false);
    } else if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      elegir(resaltada);
    } else if (['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(e.key)) {
      e.preventDefault();
      setResaltada((i) => {
        if (e.key === 'Home') return 0;
        if (e.key === 'End') return total - 1;
        return (i + (e.key === 'ArrowDown' ? 1 : -1) + total) % total;
      });
    } else if (e.key.length === 1 && !e.ctrlKey && !e.metaKey && !e.altKey) {
      // Escribir una letra salta a la siguiente opción que empieza con ella
      const letra = e.key.toLowerCase();
      for (let paso = 1; paso <= total; paso++) {
        const i = (resaltada + paso) % total;
        if (opciones[i].texto.toLowerCase().startsWith(letra)) {
          setResaltada(i);
          break;
        }
      }
    }
  };

  const esquina = `${lugar.arriba ? 'bottom' : 'top'} ${lugar.derecha ? 'right' : 'left'}`;

  return (
    <div ref={caja} className="relative inline-flex max-w-full">
      <button
        ref={ref}
        type="button"
        role="combobox"
        aria-label={`${etiqueta}. Actual: ${texto}`}
        aria-haspopup="listbox"
        aria-expanded={abierto}
        aria-controls={abierto ? idMenu : undefined}
        aria-activedescendant={abierto ? `${idMenu}-${resaltada}` : undefined}
        disabled={deshabilitado}
        onClick={() => (abierto ? setAbierto(false) : abrir())}
        onKeyDown={alPulsar}
        className="inline-flex min-h-11 max-w-full cursor-pointer items-center gap-2 rounded-full bg-forest-900 py-1 pl-4 pr-3 text-xs font-semibold text-white transition-colors duration-150 hover:bg-forest-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 focus-visible:ring-offset-2 focus-visible:ring-offset-surface disabled:cursor-default disabled:opacity-60"
      >
        <AnimatePresence mode="popLayout" initial={false}>
          <motion.span
            key={texto}
            className="truncate"
            initial={sinMovimiento ? false : { opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={sinMovimiento ? undefined : { opacity: 0, y: -10 }}
            transition={{ duration: 0.22, ease: 'easeOut' }}
          >
            {texto}
          </motion.span>
        </AnimatePresence>
        <svg
          aria-hidden="true"
          viewBox="0 0 16 16"
          className={`h-3.5 w-3.5 shrink-0 fill-none stroke-sun-500 stroke-2 transition-transform duration-200 ${
            abierto ? 'rotate-180' : ''
          }`}
        >
          <path d="M3.5 6l4.5 4.5L12.5 6" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </button>

      <AnimatePresence>
        {abierto && (
          <motion.ul
            id={idMenu}
            role="listbox"
            aria-label={etiqueta}
            // El clic no le quita el foco al botón
            onPointerDown={(e) => e.preventDefault()}
            onMouseDown={(e) => e.preventDefault()}
            initial={sinMovimiento ? { opacity: 0 } : { opacity: 0, scale: 0.82 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={sinMovimiento ? { opacity: 0 } : { opacity: 0, scale: 0.92, transition: { duration: 0.12 } }}
            transition={{ type: 'spring', stiffness: 520, damping: 32 }}
            style={{ transformOrigin: esquina, width: ANCHO_MENU, padding: RELLENO_MENU }}
            className={`absolute z-30 rounded-xl border border-line bg-surface shadow-[0_18px_40px_-16px_rgba(15,26,21,0.55)] ${
              lugar.arriba ? 'bottom-full mb-2' : 'top-full mt-2'
            } ${lugar.derecha ? 'right-0' : 'left-0'}`}
          >
            <motion.span
              aria-hidden="true"
              role="presentation"
              className="absolute rounded-lg bg-moss-500/20"
              style={{ left: RELLENO_MENU, right: RELLENO_MENU, top: RELLENO_MENU, height: ALTO_OPCION }}
              initial={false}
              animate={{ y: resaltada * ALTO_OPCION }}
              transition={sinMovimiento ? { duration: 0 } : { type: 'spring', stiffness: 620, damping: 40 }}
            />
            {opciones.map((o, i) => (
              <li
                key={o.valor}
                id={`${idMenu}-${i}`}
                role="option"
                aria-selected={o.valor === valor}
                onPointerMove={() => setResaltada(i)}
                onClick={() => elegir(i)}
                style={{ height: ALTO_OPCION }}
                className="relative flex cursor-pointer select-none items-center justify-between gap-2 px-3 text-[13px] font-medium text-ink"
              >
                <span className="truncate">{o.texto}</span>
                {o.valor === valor && (
                  <svg
                    aria-hidden="true"
                    viewBox="0 0 16 16"
                    className="h-4 w-4 shrink-0 fill-none stroke-moss-500 stroke-2"
                  >
                    <path d="M3 8.5l3.2 3.2L13 5" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                )}
              </li>
            ))}
          </motion.ul>
        )}
      </AnimatePresence>
    </div>
  );
}
