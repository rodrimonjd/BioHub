package co.edu.unbosque.biohub.consulta.lectura;

import co.edu.unbosque.biohub.consulta.especie.EspecieInfo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BusquedaHallazgos {

    public static final Sort ORDEN = Sort.by(Sort.Order.desc("fecha"), Sort.Order.asc("id"));

    private static final char ESCAPE = '!';

    private final List<Criterio> criterios = new ArrayList<>();

    private interface Criterio {
        Predicate aplicar(Root<HallazgoLectura> h, CriteriaBuilder cb);
    }

    public BusquedaHallazgos especieId(String especieId) {
        if (especieId != null) criterios.add((h, cb) -> cb.equal(h.get("especieId"), especieId));
        return this;
    }

    public BusquedaHallazgos departamento(String departamento) {
        if (departamento != null) {
            criterios.add((h, cb) -> cb.equal(cb.lower(h.get("departamento")), departamento.toLowerCase(Locale.ROOT)));
        }
        return this;
    }

    public BusquedaHallazgos desde(Instant desde) {
        if (desde != null) criterios.add((h, cb) -> cb.greaterThanOrEqualTo(h.get("fecha"), desde));
        return this;
    }

    public BusquedaHallazgos hasta(Instant hasta) {
        if (hasta != null) criterios.add((h, cb) -> cb.lessThan(h.get("fecha"), hasta));
        return this;
    }

    public BusquedaHallazgos especie(String nombre, List<EspecieInfo> porNombreComun) {
        criterios.add((h, cb) -> alguno(cb, conEspecies(h, cb, porNombreComun, contiene(h, cb, "especie", nombre))));
        return this;
    }

    public BusquedaHallazgos palabra(String palabra, List<EspecieInfo> porNombreComun) {
        criterios.add((h, cb) -> alguno(cb, conEspecies(h, cb, porNombreComun,
                contiene(h, cb, "especie", palabra),
                contiene(h, cb, "descripcion", palabra),
                contiene(h, cb, "departamento", palabra))));
        return this;
    }

    public Specification<HallazgoLectura> especificacion() {
        return (h, consulta, cb) -> cb.and(criterios.stream().map(c -> c.aplicar(h, cb)).toArray(Predicate[]::new));
    }

    private static List<Predicate> conEspecies(Root<HallazgoLectura> h, CriteriaBuilder cb,
                                               List<EspecieInfo> especies, Predicate... textos) {
        List<Predicate> opciones = new ArrayList<>(List.of(textos));
        for (EspecieInfo e : especies) {
            opciones.add(cb.equal(h.get("especieId"), e.id()));
            opciones.add(cb.equal(cb.lower(h.get("especie")), e.nombreCientifico().toLowerCase(Locale.ROOT)));
        }
        return opciones;
    }

    private static Predicate alguno(CriteriaBuilder cb, List<Predicate> opciones) {
        return cb.or(opciones.toArray(Predicate[]::new));
    }

    private static Predicate contiene(Root<HallazgoLectura> h, CriteriaBuilder cb, String campo, String texto) {
        return cb.like(cb.lower(h.get(campo)), patron(texto), ESCAPE);
    }

    static String patron(String texto) {
        String literal = texto.toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + literal + "%";
    }
}
