package co.edu.unbosque.biohub.reportes.acceso;

/** Pregunta a servicio-autorizacion si un usuario tiene un permiso (patrón Adapter). */
public interface ClienteAutorizacion {

    /**
     * @throws org.springframework.web.server.ResponseStatusException 503 si autorización no responde
     */
    boolean tienePermiso(String usuarioId, String permiso);
}
