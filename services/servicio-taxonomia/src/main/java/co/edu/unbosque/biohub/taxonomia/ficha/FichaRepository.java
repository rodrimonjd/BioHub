package co.edu.unbosque.biohub.taxonomia.ficha;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface FichaRepository extends MongoRepository<FichaTaxonomica, String> {

    List<FichaTaxonomica> findByEspecieId(String especieId);

    boolean existsByEspecieIdAndActivaTrue(String especieId);

    /** Fichas que tienen al menos un evento pendiente de publicar. */
    @Query("{ 'eventosPendientes.0': { $exists: true } }")
    List<FichaTaxonomica> buscarConEventosPendientes();
}
