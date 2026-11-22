package co.edu.unbosque.biohub.taxonomia.ficha;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.biohub.taxonomia.atributo.AtributoTaxonomico;
import co.edu.unbosque.biohub.taxonomia.compartido.DatosInvalidosException;

public class ConstructorFicha {

    private UUID especieId;
    private String autor;
    private final List<AtributoTaxonomico> atributos = new ArrayList<>();
    private final List<String> valores = new ArrayList<>();

    public ConstructorFicha especie(UUID especieId) {
        this.especieId = especieId;
        return this;
    }

    public ConstructorFicha autor(String autor) {
        this.autor = autor;
        return this;
    }

    public ConstructorFicha atributo(AtributoTaxonomico atributo, String valor) {
        atributos.add(atributo);
        valores.add(valor);
        return this;
    }

    public FichaTaxonomica build() {
        if (especieId == null) {
            throw new DatosInvalidosException("La especie es obligatoria");
        }
        if (autor == null || autor.isBlank()) {
            throw new DatosInvalidosException("El autor es obligatorio");
        }
        if (atributos.isEmpty()) {
            throw new DatosInvalidosException("La ficha debe tener al menos un atributo");
        }
        FichaTaxonomica ficha = new FichaTaxonomica(especieId.toString(), autor.trim());
        for (int i = 0; i < atributos.size(); i++) {
            ficha.agregarAtributo(atributos.get(i), valores.get(i));
        }
        return ficha;
    }
}
