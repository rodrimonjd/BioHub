package co.edu.unbosque.biohub.taxonomia.ficha;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface FichaRepositorio extends MongoRepository<FichaTaxonomica, String> {

    List<FichaTaxonomica> findByEspecieId(String especieId);

    boolean existsByEspecieIdAndActivaTrue(String especieId);

    @Query("{ 'eventosPendientes.0': { $exists: true } }")
    List<FichaTaxonomica> buscarConEventosPendientes();
}
