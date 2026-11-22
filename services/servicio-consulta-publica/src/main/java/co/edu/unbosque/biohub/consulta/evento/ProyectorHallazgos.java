package co.edu.unbosque.biohub.consulta.evento;

import co.edu.unbosque.biohub.consulta.especie.CatalogoEspecies;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLectura;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLecturaRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Aplica los eventos de servicio-registro al modelo de lectura.
 * Es idempotente: un evento con versión menor o igual a la guardada se ignora,
 * así que recibir el mismo evento dos veces no cambia nada.
 */
@Service
public class ProyectorHallazgos {

    private static final Logger log = LoggerFactory.getLogger(ProyectorHallazgos.class);

    private final HallazgoLecturaRepositorio repositorio;
    private final CatalogoEspecies catalogo;

    public ProyectorHallazgos(HallazgoLecturaRepositorio repositorio, CatalogoEspecies catalogo) {
        this.repositorio = repositorio;
        this.catalogo = catalogo;
    }

    @Transactional
    public void aplicar(EventoEntrante evento) {
        if (evento.idAgregado() == null) {
            throw new IllegalArgumentException("Evento sin idAgregado");
        }
        switch (evento.tipo() == null ? "" : evento.tipo()) {
            case EventoEntrante.HALLAZGO_REGISTRADO -> registrar(evento);
            case EventoEntrante.HALLAZGO_MODIFICADO -> modificar(evento);
            default -> log.debug("Evento ignorado: {}", evento.tipo());
        }
    }

    private void registrar(EventoEntrante evento) {
        Map<String, Object> d = evento.datos();
        int version = entero(d.get("version"), 1);
        Optional<HallazgoLectura> existente = repositorio.findById(evento.idAgregado());
        if (existente.isPresent() && existente.get().getVersion() >= version) {
            return; // ya aplicado
        }
        HallazgoLectura h = existente.orElseGet(() -> new HallazgoLectura(evento.idAgregado()));
        h.registrar(texto(d.get("especieId")), texto(d.get("especie")), instante(d.get("fecha")),
                texto(d.get("descripcion")), decimal(d.get("latitud")), decimal(d.get("longitud")),
                texto(d.get("departamento")), texto(d.get("region")), version);
        repositorio.save(h);
    }

    private void modificar(EventoEntrante evento) {
        Map<String, Object> d = evento.datos();
        Optional<HallazgoLectura> existente = repositorio.findById(evento.idAgregado());
        if (existente.isEmpty()) {
            log.warn("Modificación de un hallazgo desconocido {}; se ignora", evento.idAgregado());
            return;
        }
        HallazgoLectura h = existente.get();
        int version = entero(d.get("version"), h.getVersion() + 1);
        if (h.getVersion() >= version) {
            return; // ya aplicado
        }
        String campo = texto(d.get("campo"));
        String valor = texto(d.get("valorNuevo"));
        switch (campo == null ? "" : campo) {
            case "descripcion" -> h.cambiarDescripcion(valor);
            case "departamento" -> h.cambiarDepartamento(valor);
            case "region" -> h.cambiarRegion(valor);
            // registro envía el nombre científico de la nueva especie; el id se busca en el catálogo
            case "especie" -> h.reclasificar(
                    catalogo.porNombreCientifico(valor).map(e -> e.id()).orElse(null), valor);
            default -> log.warn("Campo modificado desconocido '{}' en el hallazgo {}", campo, h.getId());
        }
        h.marcarVersion(version);
    }

    // ---- conversión de los valores del JSON ----

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static Double decimal(Object valor) {
        if (valor == null) return null;
        if (valor instanceof Number n) return n.doubleValue();
        return Double.valueOf(valor.toString());
    }

    private static int entero(Object valor, int porDefecto) {
        if (valor instanceof Number n) return n.intValue();
        if (valor != null) return Integer.parseInt(valor.toString());
        return porDefecto;
    }

    /** Acepta fecha ISO-8601 ("2026-11-21T10:00:00Z") o segundos desde 1970 (con decimales). */
    private static Instant instante(Object valor) {
        if (valor == null) throw new IllegalArgumentException("Evento sin fecha");
        if (valor instanceof Number n) {
            BigDecimal segundos = new BigDecimal(n.toString());
            return Instant.ofEpochSecond(segundos.longValue(),
                    segundos.remainder(BigDecimal.ONE).movePointRight(9).longValue());
        }
        return Instant.parse(valor.toString());
    }
}
