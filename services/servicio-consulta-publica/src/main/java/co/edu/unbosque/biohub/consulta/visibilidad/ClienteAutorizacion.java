package co.edu.unbosque.biohub.consulta.visibilidad;

public interface ClienteAutorizacion {

    NivelVisibilidad visibilidad(String usuarioId, String categoriaConservacion);
}
