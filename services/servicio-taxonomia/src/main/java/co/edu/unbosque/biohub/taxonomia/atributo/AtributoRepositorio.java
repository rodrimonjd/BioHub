package co.edu.unbosque.biohub.taxonomia.atributo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AtributoRepositorio extends MongoRepository<AtributoTaxonomico, String> {

    Optional<AtributoTaxonomico> findByNombreIgnoreCase(String nombre);
}
