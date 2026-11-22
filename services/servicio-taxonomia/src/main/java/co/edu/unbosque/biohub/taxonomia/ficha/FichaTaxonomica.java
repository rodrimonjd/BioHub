package co.edu.unbosque.biohub.taxonomia.ficha;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import co.edu.unbosque.biohub.taxonomia.atributo.AtributoTaxonomico;
import co.edu.unbosque.biohub.taxonomia.compartido.DatosInvalidosException;
import co.edu.unbosque.biohub.taxonomia.evento.EventoPendiente;

@Document("fichas")
public class FichaTaxonomica {

    @Id
    private String id;
    private String especieId;
    private int version;
    private boolean activa;
    private List<ValorAtributo> atributos = new ArrayList<>();
    private String creadoPor;
    private Instant creadoEn;
    private Instant actualizadoEn;

    @JsonIgnore
    private List<EventoPendiente> eventosPendientes = new ArrayList<>();

    @Version
    @JsonIgnore
    private Long revision;

    protected FichaTaxonomica() { }

    FichaTaxonomica(String especieId, String creadoPor) {
        this.id = UUID.randomUUID().toString();
        this.especieId = especieId;
        this.creadoPor = creadoPor;
        this.version = 1;
        this.activa = true;
        this.creadoEn = Instant.now();
        this.actualizadoEn = this.creadoEn;
    }

    public void agregarAtributo(AtributoTaxonomico atributo, String valor) {
        if (buscar(atributo.getNombre()) != null) {
            throw new DatosInvalidosException("La ficha ya tiene el atributo: " + atributo.getNombre());
        }
        validar(atributo, valor);
        atributos.add(new ValorAtributo(atributo, limpiar(valor)));
    }

    public void incorporarAtributo(AtributoTaxonomico atributo, String valor) {
        exigirActiva();
        agregarAtributo(atributo, valor);
        subirVersion();
    }

    public String modificarValor(String nombre, String valor) {
        exigirActiva();
        ValorAtributo actual = buscar(nombre);
        if (actual == null) {
            throw new DatosInvalidosException("La ficha no tiene el atributo: " + nombre);
        }
        validar(actual.getAtributo(), valor);
        String anterior = actual.getValor();
        String nuevo = limpiar(valor);
        if (Objects.equals(anterior, nuevo)) {
            throw new DatosInvalidosException("El nuevo valor es igual al actual");
        }
        actual.cambiarValor(nuevo);
        subirVersion();
        return anterior;
    }

    public void desactivar() {
        if (!activa) {
            throw new DatosInvalidosException("La ficha ya esta inactiva");
        }
        activa = false;
        subirVersion();
    }

    public String valorDe(String nombre) {
        ValorAtributo actual = buscar(nombre);
        return actual == null ? null : actual.getValor();
    }

    public void agregarEvento(EventoPendiente evento) {
        eventosPendientes.add(evento);
    }

    /** Quita los primeros eventos, que ya se publicaron. */
    public void quitarEventosPublicados(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            eventosPendientes.remove(0);
        }
    }

    private void subirVersion() {
        version++;
        actualizadoEn = Instant.now();
    }

    private void exigirActiva() {
        if (!activa) {
            throw new DatosInvalidosException("La ficha esta inactiva y no se puede modificar");
        }
    }

    private static void validar(AtributoTaxonomico atributo, String valor) {
        if (atributo.validarValor(valor)) {
            return;
        }
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El atributo " + atributo.getNombre() + " es obligatorio");
        }
        throw new DatosInvalidosException("Valor invalido para el atributo " + atributo.getNombre() + " (" + atributo.getTipoDato() + "): " + valor);
    }

    private static String limpiar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private ValorAtributo buscar(String nombre) {
        for (ValorAtributo actual : atributos) {
            if (actual.getAtributo().getNombre().equalsIgnoreCase(nombre.trim())) {
                return actual;
            }
        }
        return null;
    }

    public String getId() { return id; }
    public String getEspecieId() { return especieId; }
    public int getVersion() { return version; }
    public boolean isActiva() { return activa; }
    public List<ValorAtributo> getAtributos() { return atributos; }
    public String getCreadoPor() { return creadoPor; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public List<EventoPendiente> getEventosPendientes() { return eventosPendientes; }
}
