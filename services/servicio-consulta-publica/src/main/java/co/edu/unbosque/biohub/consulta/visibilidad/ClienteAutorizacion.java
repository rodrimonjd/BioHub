package co.edu.unbosque.biohub.consulta.visibilidad;

/** Pregunta a servicio-autorizacion con qué detalle puede ver la ubicación un usuario. */
public interface ClienteAutorizacion {

    /**
     * @param usuarioId            null o vacío = usuario anónimo
     * @param categoriaConservacion categoría UICN de la especie (CR, EN, VU, NT, LC...); puede ser null
     */
    NivelVisibilidad visibilidad(String usuarioId, String categoriaConservacion);
}
