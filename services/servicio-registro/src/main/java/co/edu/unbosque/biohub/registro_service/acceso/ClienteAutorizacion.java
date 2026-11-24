package co.edu.unbosque.biohub.registro_service.acceso;

import java.util.Set;

public interface ClienteAutorizacion {

    boolean tienePermiso(String usuarioId, String permiso);

    Set<String> especiesConUbicacionExacta(String usuarioId);
}
