'use client';

import React, { useEffect, useMemo, useRef, useState } from 'react';
import { motion, MotionConfig } from 'motion/react';

const POR_TANDA = 15;

export interface ListaDesplazableProps<T> {
  items: T[];
  /** Nombre de la lista para lectores de pantalla. */
  etiqueta: string;
  clave: (item: T) => string;
  /** Título bajo el que se agrupa el elemento; los elementos de un mismo grupo deben venir seguidos. */
  grupo: (item: T) => string;
  children: (item: T) => React.ReactNode;
  className?: string;
}

/**
 * Lista con scroll propio: los elementos aparecen con una animación la primera vez que se ven
 * y se van agregando por tandas al bajar, sin botón de "cargar más".
 */
export function ListaDesplazable<T>({
  items,
  etiqueta,
  clave,
  grupo,
  children,
  className = '',
}: ListaDesplazableProps<T>) {
  const contenedor = useRef<HTMLDivElement>(null);
  const centinela = useRef<HTMLDivElement>(null);
  const [cantidad, setCantidad] = useState(POR_TANDA);
  const [hayMas, setHayMas] = useState({ arriba: false, abajo: false });

  const visibles = useMemo(() => items.slice(0, cantidad), [items, cantidad]);
  const faltan = cantidad < items.length;

  const grupos = useMemo(() => {
    const totales = new Map<string, number>();
    for (const item of items) totales.set(grupo(item), (totales.get(grupo(item)) ?? 0) + 1);
    const lista: { titulo: string; total: number; items: T[] }[] = [];
    for (const item of visibles) {
      const titulo = grupo(item);
      const ultimo = lista[lista.length - 1];
      if (ultimo?.titulo === titulo) ultimo.items.push(item);
      else lista.push({ titulo, total: totales.get(titulo) ?? 1, items: [item] });
    }
    return lista;
  }, [items, visibles, grupo]);

  // Siguiente tanda cuando el final de la lista se acerca
  useEffect(() => {
    if (!faltan || !centinela.current) return;
    const observador = new IntersectionObserver(
      ([entrada]) => {
        if (entrada.isIntersecting) setCantidad((prev) => prev + POR_TANDA);
      },
      { root: contenedor.current, rootMargin: '400px 0px' }
    );
    observador.observe(centinela.current);
    return () => observador.disconnect();
  }, [faltan, cantidad]);

  // Bordes desvanecidos solo mientras haya algo más por ver
  useEffect(() => {
    const el = contenedor.current;
    if (!el) return;
    const medir = () => {
      const arriba = el.scrollTop > 4;
      const abajo = el.scrollHeight - el.clientHeight - el.scrollTop > 4;
      setHayMas((prev) => (prev.arriba === arriba && prev.abajo === abajo ? prev : { arriba, abajo }));
    };
    medir();
    el.addEventListener('scroll', medir, { passive: true });
    const tamano = new ResizeObserver(medir);
    tamano.observe(el);
    if (el.firstElementChild) tamano.observe(el.firstElementChild);
    return () => {
      el.removeEventListener('scroll', medir);
      tamano.disconnect();
    };
  }, []);

  // Un registro nuevo entra arriba: se sube para que se vea
  const primera = items.length > 0 ? clave(items[0]) : null;
  const primeraAnterior = useRef(primera);
  useEffect(() => {
    if (primeraAnterior.current !== null && primera !== primeraAnterior.current) {
      contenedor.current?.scrollTo({ top: 0, behavior: 'smooth' });
    }
    primeraAnterior.current = primera;
  }, [primera]);

  return (
    <MotionConfig reducedMotion="user">
      <div className={`relative ${className}`}>
        <div
          ref={contenedor}
          role="region"
          aria-label={etiqueta}
          tabIndex={0}
          className="h-full max-h-[inherit] overflow-y-auto rounded-lg pr-2 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-moss-500"
        >
          <div className="pb-1">
            {grupos.map((g) => (
              <section key={g.titulo} aria-label={g.titulo}>
                <div className="sticky top-0 z-10 bg-surface pb-2 pt-1">
                  <p className="flex items-center gap-3 font-mono text-[11px] font-semibold uppercase tracking-wider text-moss-500">
                    {g.titulo}
                    <span className="h-px flex-1 bg-line" aria-hidden="true" />
                    <span className="text-ink-muted">{g.total}</span>
                  </p>
                </div>
                <ul className="space-y-3 pb-4">
                  {g.items.map((item) => (
                    <motion.li
                      key={clave(item)}
                      initial={{ opacity: 0, y: 22, scale: 0.96 }}
                      whileInView={{ opacity: 1, y: 0, scale: 1 }}
                      viewport={{ once: true, root: contenedor, amount: 'some' }}
                      transition={{ type: 'spring', duration: 0.55, bounce: 0.22 }}
                    >
                      {children(item)}
                    </motion.li>
                  ))}
                </ul>
              </section>
            ))}
            {faltan && <div ref={centinela} className="h-px" aria-hidden="true" />}
          </div>
        </div>

        <div
          aria-hidden="true"
          className={`pointer-events-none absolute inset-x-0 bottom-0 h-16 rounded-b-lg bg-gradient-to-t from-surface to-transparent transition-opacity duration-200 ${
            hayMas.abajo ? 'opacity-100' : 'opacity-0'
          }`}
        />
        <div
          aria-hidden="true"
          className={`pointer-events-none absolute inset-x-0 top-7 z-10 h-6 bg-gradient-to-b from-surface to-transparent transition-opacity duration-200 ${
            hayMas.arriba ? 'opacity-100' : 'opacity-0'
          }`}
        />
      </div>
    </MotionConfig>
  );
}
