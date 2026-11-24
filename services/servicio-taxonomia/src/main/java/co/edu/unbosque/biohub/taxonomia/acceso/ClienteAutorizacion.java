package co.edu.unbosque.biohub.taxonomia.acceso;

public interface ClienteAutorizacion {
    boolean tienePermiso(String usuarioId, String permiso);
}
