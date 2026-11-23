package co.edu.unbosque.biohub.historial.permiso;

public interface ClienteAutorizacion {

    boolean tienePermiso(String usuarioId, String permiso);
}
