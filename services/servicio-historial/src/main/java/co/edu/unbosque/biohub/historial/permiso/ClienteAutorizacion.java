package co.edu.unbosque.biohub.historial.permiso;

/** Pregunta a servicio-autorizacion si un usuario tiene un permiso (patrón Adapter). */
public interface ClienteAutorizacion {

    /**
     * @throws co.edu.unbosque.biohub.historial.compartido.ServicioNoDisponibleException si autorización no responde
     */
    boolean tienePermiso(String usuarioId, String permiso);
}
