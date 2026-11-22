package co.edu.unbosque.biohub.consulta.especie;

import java.util.Optional;

public interface CatalogoEspecies {

    Optional<EspecieInfo> porId(String especieId);

    Optional<EspecieInfo> porNombreCientifico(String nombreCientifico);
}
