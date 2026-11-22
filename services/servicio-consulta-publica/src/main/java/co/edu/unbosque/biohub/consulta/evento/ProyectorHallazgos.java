package co.edu.unbosque.biohub.consulta.evento;

import co.edu.unbosque.biohub.consulta.especie.CatalogoEspecies;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLectura;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLecturaRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
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

    /** Qué pasó con el evento, para dejarlo claro en el log. */
    public enum Resultado { APLICADO, IGNORADO }

    /**
     * @throws EventoInvalidoException si al evento le faltan datos o vienen mal formados
     */
    @Transactional
    public Resultado aplicar(EventoEntrante evento) {
        if (evento.idAgregado() == null || evento.idAgregado().isBlank()) {
            throw new EventoInvalidoException("Evento sin idAgregado");
        }
        return switch (evento.tipo() == null ? "" : evento.tipo()) {
            case EventoEntrante.HALLAZGO_REGISTRADO -> registrar(evento);
            case EventoEntrante.HALLAZGO_MODIFICADO -> modificar(evento);
            default -> Resultado.IGNORADO;
        };
    }

    private Resultado registrar(EventoEntrante evento) {
        Map<String, Object> d = evento.datos();
        int version = entero(d.get("version"), 1);
        Optional<HallazgoLectura> existente = repositorio.findById(evento.idAgregado());
        if (existente.isPresent() && existente.get().getVersion() >= version) {
            return Resultado.IGNORADO; // ya aplicado
        }
        HallazgoLectura h = existente.orElseGet(() -> new HallazgoLectura(evento.idAgregado()));
        h.registrar(texto(d.get("especieId")), requerido(d, "especie"), instante(d.get("fecha")),
                requerido(d, "descripcion"), decimal(d.get("latitud")), decimal(d.get("longitud")),
                requerido(d, "departamento"), texto(d.get("region")), version);
        repositorio.save(h);
        return Resultado.APLICADO;
    }

    private Resultado modificar(EventoEntrante evento) {
        Map<String, Object> d = evento.datos();
        Optional<HallazgoLectura> existente = repositorio.findById(evento.idAgregado());
        if (existente.isEmpty()) {
            log.warn("Modificación de un hallazgo desconocido {}; se ignora", evento.idAgregado());
            return Resultado.IGNORADO;
        }
        HallazgoLectura h = existente.get();
        int version = entero(d.get("version"), h.getVersion() + 1);
        if (h.getVersion() >= version) {
            return Resultado.IGNORADO; // ya aplicado
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
        return Resultado.APLICADO;
    }

    // ---- conversión de los valores del JSON (cualquier valor mal formado = evento inválido) ----

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static String requerido(Map<String, Object> datos, String campo) {
        String valor = texto(datos.get(campo));
        if (valor == null || valor.isBlank()) {
            throw new EventoInvalidoException("Evento sin el campo obligatorio '" + campo + "'");
        }
        return valor;
    }

    private static Double decimal(Object valor) {
        if (valor == null) return null;
        if (valor instanceof Number n) return n.doubleValue();
        try {
            return Double.valueOf(valor.toString());
        } catch (NumberFormatException e) {
            throw new EventoInvalidoException("Número inválido: " + valor, e);
        }
    }

    private static int entero(Object valor, int porDefecto) {
        if (valor instanceof Number n) return n.intValue();
        if (valor == null) return porDefecto;
        try {
            return Integer.parseInt(valor.toString());
        } catch (NumberFormatException e) {
            throw new EventoInvalidoException("Entero inválido: " + valor, e);
        }
    }

    /** Acepta fecha ISO-8601 ("2026-11-21T10:00:00Z") o segundos desde 1970 (con decimales). */
    private static Instant instante(Object valor) {
        if (valor == null) throw new EventoInvalidoException("Evento sin el campo obligatorio 'fecha'");
        try {
            if (valor instanceof Number n) {
                BigDecimal segundos = new BigDecimal(n.toString());
                return Instant.ofEpochSecond(segundos.longValue(),
                        segundos.remainder(BigDecimal.ONE).movePointRight(9).longValue());
            }
            return Instant.parse(valor.toString());
        } catch (DateTimeException | ArithmeticException e) {
            throw new EventoInvalidoException("Fecha inválida: " + valor, e);
        }
    }
}
