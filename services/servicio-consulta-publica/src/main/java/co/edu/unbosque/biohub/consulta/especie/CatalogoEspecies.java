package co.edu.unbosque.biohub.consulta.especie;

import java.util.Optional;

/**
 * Catálogo de especies (patrón Adapter): hoy se obtiene por HTTP de servicio-registro;
 * si más adelante registro publica eventos de especies, basta con otra implementación.
 */
public interface CatalogoEspecies {

    Optional<EspecieInfo> porId(String especieId);

    Optional<EspecieInfo> porNombreCientifico(String nombreCientifico);
}
