package co.edu.unbosque.biohub.taxonomia.atributo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AtributoService {

    private final AtributoRepository atributos;

    public AtributoService(AtributoRepository atributos) {
        this.atributos = atributos;
    }

    public AtributoTaxonomico crear(AtributoRequest r) {
        boolean obligatorio = Boolean.TRUE.equals(r.obligatorio());
        AtributoTaxonomico nuevo = AtributoFactory.crear(r.nombre(), r.tipoDato(), r.unidad(), r.opciones(), obligatorio);

        if (atributos.findByNombreIgnoreCase(nuevo.getNombre()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un atributo con el nombre: " + nuevo.getNombre());
        }
        return atributos.save(nuevo);
    }

    public List<AtributoTaxonomico> listar() {
        return atributos.findAll();
    }

    public AtributoTaxonomico buscarPorNombre(String nombre) {
        return atributos.findByNombreIgnoreCase(nombre.trim()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atributo no encontrado en el catalogo: " + nombre));
    }
}
