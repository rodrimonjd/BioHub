import type { Notificacion } from '@/components/ui/Notificaciones';
import { ErrorRechazado } from '@/lib/registro-api';

const SIN_PERMISO = /^Sin permiso:|Se requiere el permiso|no tiene permiso/i;
const SIN_CONEXION = /Failed to fetch|No se pudo conectar|No hay conexión/i;

interface TextosDeError {
  /** Título cuando el servicio rechaza la operación, por ejemplo "No se registró el hallazgo". */
  titulo: string;
  /** Qué no alcanzó a pasar, por ejemplo "El hallazgo no se guardó". */
  sinConexion: string;
  /** Qué no puede hacer el rol, por ejemplo "registrar hallazgos". */
  accion: string;
}

/** Convierte el error de un servicio en un aviso sin términos técnicos. */
export function avisoDeError(error: unknown, { titulo, sinConexion, accion }: TextosDeError): Notificacion {
  const mensaje = error instanceof Error ? error.message : '';
  if (/autorizador/i.test(mensaje)) {
    return { tipo: 'error', titulo, detalle: 'La persona elegida no puede autorizar este cambio.' };
  }
  if ((error instanceof ErrorRechazado && error.status === 403) || SIN_PERMISO.test(mensaje)) {
    return { tipo: 'error', titulo: 'No tienes permiso', detalle: `Tu rol no puede ${accion}.` };
  }
  if (error instanceof TypeError || SIN_CONEXION.test(mensaje)) {
    return { tipo: 'error', titulo: 'Sin conexión', detalle: `${sinConexion}. Inténtalo de nuevo en un momento.` };
  }
  return { tipo: 'error', titulo, detalle: mensaje || undefined };
}
