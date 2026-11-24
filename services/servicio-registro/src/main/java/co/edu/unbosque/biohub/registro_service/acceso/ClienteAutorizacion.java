package co.edu.unbosque.biohub.registro_service.acceso;

import java.util.Set;

/** Pregunta a servicio-autorizacion si un usuario tiene un permiso (patrón Adapter). */
public interface ClienteAutorizacion {

    /**
     * @throws org.springframework.web.server.ResponseStatusException 503 si autorización no responde
     */
    boolean tienePermiso(String usuarioId, String permiso);

    /**
     * Especies (ids) cuya ubicación exacta la autoridad ambiental le dio al usuario, aunque no tenga ubicacion:ver_exacta.
     *
     * @throws org.springframework.web.server.ResponseStatusException 503 si autorización no responde
     */
    Set<String> especiesConUbicacionExacta(String usuarioId);
}
