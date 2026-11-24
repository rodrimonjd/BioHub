'use client';

import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { AnimatePresence, motion, MotionConfig, PanInfo } from 'motion/react';

export type TipoNotificacion = 'ok' | 'aviso' | 'error';

export interface Notificacion {
  tipo: TipoNotificacion;
  titulo: string;
  detalle?: string;
}

type NotificacionActiva = Notificacion & { id: number };

const MAXIMO_VISIBLES = 4;

/** Milisegundos antes de cerrarse sola; 0 la deja hasta que la persona la cierre. */
const DURACION: Record<TipoNotificacion, number> = { ok: 5000, aviso: 8000, error: 0 };

const COLOR: Record<TipoNotificacion, string> = {
  ok: 'var(--color-iucn-lc)',
  aviso: 'var(--color-sun-500)',
  error: 'var(--color-iucn-cr)',
};

const TRAZOS: Record<TipoNotificacion, string[]> = {
  ok: ['M7.5 12.4 L10.7 15.6 L16.6 8.9'],
  aviso: ['M12 7.4 L12 13', 'M12 16.4 L12 16.5'],
  error: ['M8.6 8.6 L15.4 15.4', 'M15.4 8.6 L8.6 15.4'],
};

const Contexto = createContext<((n: Notificacion) => void) | null>(null);

/** Muestra un aviso flotante: `notificar({ tipo: 'ok', titulo: 'Especie agregada', detalle: '...' })`. */
export function useNotificar(): (n: Notificacion) => void {
  const notificar = useContext(Contexto);
  if (!notificar) throw new Error('useNotificar debe usarse dentro de NotificacionesProvider');
  return notificar;
}

export function NotificacionesProvider({ children }: { children: React.ReactNode }) {
  const [activas, setActivas] = useState<NotificacionActiva[]>([]);
  const siguienteId = useRef(1);

  const notificar = useCallback((n: Notificacion) => {
    const nueva = { ...n, id: siguienteId.current++ };
    setActivas((prev) => [...prev, nueva].slice(-MAXIMO_VISIBLES));
  }, []);

  const cerrar = useCallback((id: number) => {
    setActivas((prev) => prev.filter((n) => n.id !== id));
  }, []);

  return (
    <Contexto.Provider value={notificar}>
      {children}
      <MotionConfig reducedMotion="user">
        <section
          aria-label="Notificaciones"
          className="pointer-events-none fixed inset-x-4 bottom-4 z-50 flex flex-col items-end gap-3 sm:left-auto sm:right-6 sm:bottom-6"
        >
          <AnimatePresence mode="popLayout" initial={false}>
            {activas.map((n) => (
              <Tarjeta key={n.id} notificacion={n} onCerrar={cerrar} />
            ))}
          </AnimatePresence>
        </section>
      </MotionConfig>
    </Contexto.Provider>
  );
}

interface TarjetaProps {
  notificacion: NotificacionActiva;
  onCerrar: (id: number) => void;
  ref?: React.Ref<HTMLDivElement>;
}

function Tarjeta({ notificacion, onCerrar, ref }: TarjetaProps) {
  const { id, tipo, titulo, detalle } = notificacion;
  const duracion = DURACION[tipo];
  const mecha = useRef<HTMLSpanElement>(null);
  const animacion = useRef<Animation | null>(null);
  const [enPausa, setEnPausa] = useState(false);

  // La línea inferior marca el tiempo y es la que cierra el aviso al terminar
  useEffect(() => {
    if (!duracion || !mecha.current) return;
    const a = mecha.current.animate([{ transform: 'scaleX(1)' }, { transform: 'scaleX(0)' }], {
      duration: duracion,
      easing: 'linear',
      fill: 'forwards',
    });
    a.onfinish = () => onCerrar(id);
    animacion.current = a;

    const alCambiarPestana = () => (document.hidden ? a.pause() : a.play());
    document.addEventListener('visibilitychange', alCambiarPestana);
    return () => {
      document.removeEventListener('visibilitychange', alCambiarPestana);
      a.onfinish = null;
      a.cancel();
    };
  }, [duracion, id, onCerrar]);

  useEffect(() => {
    const a = animacion.current;
    if (!a || a.playState === 'finished') return;
    if (enPausa) a.pause();
    else if (!document.hidden) a.play();
  }, [enPausa]);

  const alSoltar = (_: unknown, info: PanInfo) => {
    if (info.offset.x > 90 || info.velocity.x > 500) onCerrar(id);
  };

  return (
    <motion.div
      ref={ref}
      layout
      role={tipo === 'error' ? 'alert' : 'status'}
      initial={{ opacity: 0, y: 48, scale: 0.94 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, x: 120, transition: { duration: 0.22, ease: [0.4, 0, 1, 1] } }}
      transition={{ type: 'spring', duration: 0.5, bounce: 0.28 }}
      drag="x"
      dragConstraints={{ left: 0, right: 0 }}
      dragElastic={{ left: 0.04, right: 0.9 }}
      onDragEnd={alSoltar}
      onPointerEnter={() => setEnPausa(true)}
      onPointerLeave={() => setEnPausa(false)}
      onFocus={() => setEnPausa(true)}
      onBlur={() => setEnPausa(false)}
      style={{
        ['--tono' as string]: COLOR[tipo],
        borderColor: 'color-mix(in srgb, var(--tono) 50%, var(--color-line))',
        touchAction: 'pan-y',
      }}
      className="pointer-events-auto relative w-full cursor-grab select-none overflow-hidden rounded-xl border bg-surface shadow-[0_18px_40px_-12px_rgba(15,26,21,0.45)] active:cursor-grabbing sm:w-96"
    >
      <span
        aria-hidden="true"
        className="pointer-events-none absolute inset-0"
        style={{ background: 'linear-gradient(100deg, color-mix(in srgb, var(--tono) 16%, transparent), transparent 55%)' }}
      />

      <div className="relative flex items-start gap-3.5 py-4 pl-4 pr-3">
        <Icono tipo={tipo} />
        <div className="min-w-0 flex-1 pt-0.5">
          <p className="text-sm font-semibold text-ink">{titulo}</p>
          {detalle && <p className="mt-0.5 text-[13px] leading-snug text-ink-muted">{detalle}</p>}
        </div>
        <button
          type="button"
          aria-label="Cerrar aviso"
          onClick={() => onCerrar(id)}
          className="flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-md text-ink-muted transition-colors hover:bg-line/60 hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-moss-500"
        >
          <svg viewBox="0 0 16 16" className="h-3.5 w-3.5" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" aria-hidden="true">
            <path d="M3.5 3.5 L12.5 12.5 M12.5 3.5 L3.5 12.5" />
          </svg>
        </button>
      </div>

      {duracion > 0 && (
        <span
          ref={mecha}
          aria-hidden="true"
          className="absolute inset-x-0 bottom-0 h-[3px] origin-left"
          style={{ background: 'var(--tono)' }}
        />
      )}
    </motion.div>
  );
}

function Icono({ tipo }: { tipo: TipoNotificacion }) {
  return (
    <svg
      viewBox="0 0 24 24"
      className="h-9 w-9 shrink-0"
      fill="none"
      stroke="var(--tono)"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <motion.circle
        cx="12"
        cy="12"
        r="10"
        strokeWidth="1.6"
        style={{ fill: 'color-mix(in srgb, var(--tono) 14%, transparent)' }}
        initial={{ pathLength: 0 }}
        animate={{ pathLength: 1 }}
        transition={{ duration: 0.45, ease: 'easeOut', delay: 0.1 }}
      />
      {TRAZOS[tipo].map((d, i) => (
        <motion.path
          key={d}
          d={d}
          strokeWidth="2.1"
          initial={{ pathLength: 0, opacity: 0 }}
          animate={{ pathLength: 1, opacity: 1 }}
          transition={{
            pathLength: { duration: 0.28, ease: 'easeOut', delay: 0.42 + i * 0.16 },
            opacity: { duration: 0.01, delay: 0.42 + i * 0.16 },
          }}
        />
      ))}
    </svg>
  );
}
