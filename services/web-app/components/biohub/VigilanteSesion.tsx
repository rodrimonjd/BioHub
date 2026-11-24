'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useNotificar } from '@/components/ui/Notificaciones';
import { getStoredSession } from '@/lib/permissions';
import { EVENTO_SESION_VENCIDA } from '@/lib/pedir';
import { borrarCookieSesion, cerrarSesion } from '@/lib/sesion';

/** Cuando un servicio rechaza el token, cierra la sesión y lleva a iniciarla de nuevo. */
export function VigilanteSesion() {
  const router = useRouter();
  const notificar = useNotificar();

  useEffect(() => {
    const vencida = () => {
      const sesion = getStoredSession();
      if (!sesion || sesion.invitado) {
        borrarCookieSesion();
        return;
      }
      cerrarSesion();
      notificar({
        tipo: 'aviso',
        titulo: 'Tu sesión venció',
        detalle: 'Vuelve a iniciar sesión para continuar.',
      });
      router.replace('/login');
    };
    window.addEventListener(EVENTO_SESION_VENCIDA, vencida);
    return () => window.removeEventListener(EVENTO_SESION_VENCIDA, vencida);
  }, [notificar, router]);

  return null;
}
