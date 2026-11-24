'use client';

import React, { useEffect, useId, useRef, useState } from 'react';
import { AnimatePresence, motion, useReducedMotion } from 'motion/react';
import { BotonMantener } from '@/components/ui/BotonMantener';
import { SelectorDeslizante } from '@/components/ui/SelectorDeslizante';
import { VistaRol, VistaUsuario } from '@/lib/autorizacion-api';
import { etiquetaRoles } from '@/lib/permissions';

export interface CambioDeRolProps {
  usuario: VistaUsuario;
  roles: VistaRol[];
  /** Es la propia cuenta de quien administra: su rol de administrador no se puede quitar. */
  bloqueado: boolean;
  /** Hay un cambio guardándose, de esta cuenta o de otra. */
  ocupado: boolean;
  /** Devuelve si el cambio se guardó. */
  onCambiar: (rol: string) => Promise<boolean>;
}

/** El rol actual de la cuenta es el botón: al elegir otro se pide confirmar manteniendo presionado. */
export function CambioDeRol({ usuario, roles, bloqueado, ocupado, onCambiar }: CambioDeRolProps) {
  const [nuevo, setNuevo] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);
  const boton = useRef<HTMLButtonElement>(null);
  const devolverFoco = useRef(false);
  const idCambio = useId();
  const sinMovimiento = useReducedMotion();
  const deshabilitado = ocupado || guardando;

  // El botón recupera el foco cuando vuelve a estar habilitado
  useEffect(() => {
    if (deshabilitado || !devolverFoco.current) return;
    devolverFoco.current = false;
    boton.current?.focus({ preventScroll: true });
  }, [deshabilitado]);

  const variosRoles = usuario.roles.length > 1;

  if (bloqueado) {
    return (
      <div className="space-y-1">
        <span className="inline-flex min-h-11 items-center gap-2 rounded-full border border-line px-4 text-xs font-semibold text-ink">
          <svg
            aria-hidden="true"
            viewBox="0 0 16 16"
            className="h-3.5 w-3.5 shrink-0 fill-none stroke-ink-muted stroke-[1.5]"
          >
            <rect x="3" y="7" width="10" height="7" rx="1.5" />
            <path d="M5.5 7V5a2.5 2.5 0 0 1 5 0v2" strokeLinecap="round" />
          </svg>
          {usuario.roles.map((r) => etiquetaRoles([r])).join(' · ')}
        </span>
        <p className="text-xs text-ink-muted">Tu propio rol de administrador no se puede cambiar.</p>
      </div>
    );
  }

  const opciones = roles
    .filter((r) => variosRoles || !usuario.roles.includes(r.nombre))
    .map((r) => ({ valor: r.nombre, texto: etiquetaRoles([r.nombre]) }));

  const confirmar = async () => {
    if (!nuevo) return;
    setGuardando(true);
    const guardado = await onCambiar(nuevo);
    devolverFoco.current = guardado;
    setGuardando(false);
    if (guardado) setNuevo(null);
  };

  return (
    <div className="space-y-2">
      {variosRoles && (
        <p className="text-xs text-ink-muted">
          Esta cuenta tiene más de un rol. Elige con cuál se queda.
        </p>
      )}
      <SelectorDeslizante
        ref={boton}
        etiqueta={`Cambiar el rol de ${usuario.nombre}`}
        texto={usuario.roles.map((r) => etiquetaRoles([r])).join(' · ')}
        opciones={opciones}
        valor={nuevo}
        onElegir={setNuevo}
        deshabilitado={deshabilitado}
      />
      <AnimatePresence initial={false}>
        {nuevo && (
          <motion.div
            key="confirmar"
            role="group"
            aria-labelledby={idCambio}
            initial={sinMovimiento ? { opacity: 0 } : { opacity: 0, y: -6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, transition: { duration: 0.12 } }}
            transition={{ duration: 0.22, ease: 'easeOut' }}
            className="space-y-2 rounded-xl border border-line bg-paper p-3"
          >
            <p id={idCambio} aria-live="polite" className="text-xs text-ink-muted">
              Pasa a <span className="font-semibold text-ink">{etiquetaRoles([nuevo])}</span>
              {variosRoles ? ' y pierde los demás roles' : ` y deja de ser ${etiquetaRoles(usuario.roles)}`}
            </p>
            <div className="flex flex-wrap items-start gap-2">
              <BotonMantener onConfirmar={confirmar} ocupado={guardando} textoOcupado="Cambiando…">
                Mantén para cambiar
              </BotonMantener>
              <button
                type="button"
                disabled={guardando}
                onClick={() => {
                  setNuevo(null);
                  boton.current?.focus({ preventScroll: true });
                }}
                className="min-h-11 cursor-pointer rounded-full px-3 text-[13px] font-medium text-ink-muted transition-colors hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sun-500 disabled:opacity-50"
              >
                Cancelar
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
