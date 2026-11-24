'use client';

import React, { useEffect, useId, useRef, useState } from 'react';
import { animate, motion, useAnimationControls, useMotionValue, useReducedMotion, useTransform } from 'motion/react';

export interface BotonMantenerProps {
  children: React.ReactNode;
  /** Se llama una sola vez, cuando se mantuvo presionado el tiempo completo. */
  onConfirmar: () => void;
  /** Mientras se guarda: el botón queda lleno y no responde. */
  ocupado?: boolean;
  textoOcupado?: string;
  /** Milisegundos que hay que mantenerlo presionado. */
  duracion?: number;
  className?: string;
}

/** Un toque más corto que esto cuenta como clic: se avisa que hay que mantenerlo. */
const TOQUE_CORTO = 300;

/**
 * Botón que se confirma manteniéndolo presionado: se llena de abajo hacia arriba y, si se suelta
 * antes, se vacía. Con teclado se mantiene Enter o Espacio.
 */
export function BotonMantener({
  children,
  onConfirmar,
  ocupado = false,
  textoOcupado = 'Guardando…',
  duracion = 1000,
  className = '',
}: BotonMantenerProps) {
  const avance = useMotionValue(0);
  // Los 8 px de más esconden la ola cuando está vacío
  const altura = useTransform(avance, (v) => `calc(${(1 - v) * 100}% + ${(1 - v) * 8}px)`);
  const recorte = useTransform(avance, (v) => `inset(calc(${(1 - v) * 100}% + ${(1 - v) * 8 - 2}px) 0 0 0)`);
  const sacudida = useAnimationControls();
  const animacion = useRef<{ stop: () => void } | null>(null);
  const inicio = useRef<number | null>(null);
  const confirmado = useRef(false);
  const [presionado, setPresionado] = useState(false);
  const [aviso, setAviso] = useState('');
  const idAyuda = useId();
  const sinMovimiento = useReducedMotion();

  // Al terminar de guardar vuelve a quedar listo, por si hubo un error y se reintenta
  useEffect(() => {
    if (ocupado) {
      avance.set(1);
      return;
    }
    confirmado.current = false;
    const vaciar = animate(avance, 0, { duration: 0.25, ease: 'easeOut' });
    return () => vaciar.stop();
  }, [avance, ocupado]);

  useEffect(() => () => animacion.current?.stop(), []);

  const confirmar = () => {
    if (confirmado.current || ocupado) return;
    confirmado.current = true;
    inicio.current = null;
    setPresionado(false);
    onConfirmar();
  };

  const presionar = () => {
    if (ocupado || confirmado.current || inicio.current !== null) return;
    inicio.current = performance.now();
    setAviso('');
    setPresionado(true);
    animacion.current?.stop();
    animacion.current = animate(avance, 1, {
      duration: (duracion / 1000) * (1 - avance.get()),
      ease: 'linear',
      onComplete: confirmar,
    });
  };

  const soltar = (esClic: boolean) => {
    if (inicio.current === null) return;
    const tiempo = performance.now() - inicio.current;
    inicio.current = null;
    setPresionado(false);
    animacion.current?.stop();
    animacion.current = animate(avance, 0, { type: 'spring', stiffness: 260, damping: 30 });
    if (esClic && tiempo < TOQUE_CORTO) {
      setAviso('Mantén presionado el botón para confirmar');
      if (!sinMovimiento) sacudida.start({ x: [0, -4, 4, -3, 3, 0], transition: { duration: 0.32 } });
    }
  };

  return (
    <div className="inline-flex flex-col items-start gap-1">
      <motion.button
        type="button"
        animate={sacudida}
        whileTap={sinMovimiento || ocupado ? undefined : { scale: 0.97 }}
        aria-describedby={idAyuda}
        aria-disabled={ocupado}
        onPointerDown={(e) => {
          if (e.button !== 0) return;
          presionar();
        }}
        onPointerUp={() => soltar(true)}
        onPointerLeave={() => soltar(false)}
        onPointerCancel={() => soltar(false)}
        onContextMenu={(e) => e.preventDefault()}
        onKeyDown={(e) => {
          if (e.key !== 'Enter' && e.key !== ' ') return;
          e.preventDefault();
          if (!e.repeat) presionar();
        }}
        onKeyUp={(e) => {
          if (e.key !== 'Enter' && e.key !== ' ') return;
          e.preventDefault();
          soltar(true);
        }}
        onBlur={() => soltar(false)}
        onClick={(e) => {
          // Un lector de pantalla activa el botón sin puntero ni teclas y no puede mantenerlo
          if (e.detail === 0) confirmar();
        }}
        className={`relative inline-flex min-h-11 cursor-pointer select-none items-center justify-center overflow-hidden whitespace-nowrap rounded-full border border-forest-700 bg-surface px-4 text-[13px] font-semibold text-ink [-webkit-touch-callout:none] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 focus-visible:ring-offset-2 focus-visible:ring-offset-surface ${
          ocupado ? 'pointer-events-none' : ''
        } ${className}`}
      >
        <span>{ocupado ? textoOcupado : children}</span>

        <motion.span aria-hidden="true" className="absolute inset-0 bg-forest-700" style={{ y: altura }}>
          {!sinMovimiento && !ocupado && (
            <motion.svg
              viewBox="0 0 120 8"
              preserveAspectRatio="none"
              className="absolute bottom-[calc(100%-1px)] left-0 h-1.5 w-[200%] fill-forest-700"
              animate={presionado ? { x: ['0%', '-50%'] } : { x: '0%' }}
              transition={presionado ? { duration: 0.7, ease: 'linear', repeat: Infinity } : { duration: 0 }}
            >
              <path d="M0 8 V4 Q7.5 0 15 4 T30 4 T45 4 T60 4 T75 4 T90 4 T105 4 T120 4 V8 Z" />
            </motion.svg>
          )}
        </motion.span>

        {/* El mismo texto en blanco, visible solo donde ya llegó el relleno */}
        <motion.span
          aria-hidden="true"
          className="absolute inset-0 flex items-center justify-center px-4 text-white"
          style={{ clipPath: recorte }}
        >
          {ocupado ? textoOcupado : children}
        </motion.span>
      </motion.button>
      <span id={idAyuda} className="sr-only">
        Mantén presionado un segundo para confirmar
      </span>
      <span aria-live="polite" className={aviso ? 'text-[11px] text-ink-muted' : 'sr-only'}>
        {aviso}
      </span>
    </div>
  );
}
