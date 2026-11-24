package co.edu.unbosque.biohub.reportes.acceso;

public interface ClienteAutorizacion {

    boolean tienePermiso(String usuarioId, String permiso);
}
