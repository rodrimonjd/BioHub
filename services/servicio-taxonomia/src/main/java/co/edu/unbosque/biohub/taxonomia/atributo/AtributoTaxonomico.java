package co.edu.unbosque.biohub.taxonomia.atributo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("atributos")
public class AtributoTaxonomico {

    @Id
    private String id;
    private String nombre;
    private TipoDato tipoDato;
    private String unidad;
    private List<String> opciones;
    private boolean obligatorio;

    protected AtributoTaxonomico() { }

    AtributoTaxonomico(String nombre, TipoDato tipoDato, String unidad, List<String> opciones, boolean obligatorio) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.tipoDato = tipoDato;
        this.unidad = unidad;
        this.opciones = opciones;
        this.obligatorio = obligatorio;
    }

    public boolean validarValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return !obligatorio;
        }
        return tipoDato.esValido(valor.trim(), opciones);
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoDato getTipoDato() { return tipoDato; }
    public String getUnidad() { return unidad; }
    public List<String> getOpciones() { return opciones; }
    public boolean isObligatorio() { return obligatorio; }
}
