'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { animate } from 'motion/react';

/** Espacio que el navbar ocupa arriba al llegar a una sección (igual a scroll-padding-top en globals.css). */
const MARGEN_SUPERIOR = 80;

/** Distancia desde arriba a partir de la cual una sección cuenta como la que se está viendo. */
const LINEA_DE_LECTURA = 140;

/**
 * Sección de la página que se está viendo (por su id) y una función para ir a otra.
 * Mientras dura el salto se marca el destino, para que el indicador no pase por las intermedias.
 */
export function useSeccionActiva(ids: string[]): {
  activa: string | null;
  irA: (id: string) => boolean;
} {
  const [activa, setActiva] = useState<string | null>(null);
  const elegida = useRef<string | null>(null);
  const saltando = useRef(false);
  const cancelar = useRef<(() => void) | null>(null);
  const claveIds = ids.join('|');

  useEffect(() => {
    const lista = claveIds ? claveIds.split('|') : [];
    let cuadro = 0;

    const calcular = () => {
      cuadro = 0;
      if (saltando.current) return;
      const alFinal = window.scrollY + window.innerHeight >= document.documentElement.scrollHeight - 4;
      const posiciones = lista
        .map((id) => ({ id, top: document.getElementById(id)?.getBoundingClientRect().top }))
        .filter((s): s is { id: string; top: number } => s.top !== undefined)
        .sort((a, b) => a.top - b.top);
      const pasadas = alFinal && window.scrollY > 0
        ? posiciones.filter((s) => s.top < window.innerHeight)
        : posiciones.filter((s) => s.top <= LINEA_DE_LECTURA);
      const ultima = pasadas[pasadas.length - 1];
      if (!ultima) {
        setActiva(null);
        return;
      }
      // Secciones una al lado de la otra: se prefiere la que la persona eligió, si no la primera
      const misma = pasadas.filter((s) => Math.abs(s.top - ultima.top) < 8);
      const preferida = misma.find((s) => s.id === elegida.current) ?? misma[0];
      if (preferida.id !== elegida.current) elegida.current = null;
      setActiva(preferida.id);
    };

    const pedir = () => {
      if (!cuadro) cuadro = requestAnimationFrame(calcular);
    };
    pedir();
    window.addEventListener('scroll', pedir, { passive: true });
    const tamano = new ResizeObserver(pedir);
    tamano.observe(document.body);
    return () => {
      cancelAnimationFrame(cuadro);
      window.removeEventListener('scroll', pedir);
      tamano.disconnect();
    };
  }, [claveIds]);

  const irA = useCallback((id: string): boolean => {
    const seccion = document.getElementById(id);
    if (!seccion) return false;
    const sinAnimacion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const inicio = window.scrollY;
    elegida.current = id;
    saltando.current = true;
    setActiva(id);

    const destino = () => {
      const tope = document.documentElement.scrollHeight - window.innerHeight;
      const top = seccion.getBoundingClientRect().top + window.scrollY - MARGEN_SUPERIOR;
      return Math.max(0, Math.min(tope, top));
    };
    cancelar.current?.();
    let temporizador = 0;
    const terminar = () => {
      window.clearTimeout(temporizador);
      window.removeEventListener('wheel', detener);
      window.removeEventListener('touchstart', detener);
      window.removeEventListener('keydown', detener);
      saltando.current = false;
      cancelar.current = null;
    };
    const animacion = animate(0, 1, {
      duration: sinAnimacion ? 0 : 0.75,
      ease: [0.22, 1, 0.36, 1],
      onUpdate: (avance) => window.scrollTo(0, inicio + (destino() - inicio) * avance),
      // El navbar cambia de alto al empezar a bajar: se corrige cuando ya está quieto
      onComplete: () => {
        window.scrollTo(0, destino());
        temporizador = window.setTimeout(() => {
          window.scrollTo(0, destino());
          terminar();
        }, 340);
      },
    });
    const detener = () => {
      animacion.stop();
      terminar();
    };
    cancelar.current = detener;
    // Si la persona mueve la página, el salto la deja seguir
    window.addEventListener('wheel', detener, { passive: true });
    window.addEventListener('touchstart', detener, { passive: true });
    window.addEventListener('keydown', detener);

    // El teclado y los lectores de pantalla siguen desde la sección a la que se fue
    seccion.setAttribute('tabindex', '-1');
    seccion.style.outline = 'none';
    seccion.focus({ preventScroll: true });
    window.history.replaceState(null, '', `#${id}`);
    return true;
  }, []);

  return { activa, irA };
}
