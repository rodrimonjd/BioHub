package co.edu.unbosque.biohub.consulta.visibilidad;

public interface ClienteAutorizacion {

    /**
     * @param especieId especie del hallazgo: la autoridad ambiental puede dar acceso exacto a especies puntuales
     */
    NivelVisibilidad visibilidad(String usuarioId, String categoriaConservacion, String especieId);
}
