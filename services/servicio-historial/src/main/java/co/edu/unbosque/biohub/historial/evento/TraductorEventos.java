package co.edu.unbosque.biohub.historial.evento;

import co.edu.unbosque.biohub.historial.cambio.TipoCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TraductorEventos {

    public CambioPropuesto traducir(EventoEntrante e) {
        if (e.idEvento() == null || e.idEvento().isBlank()) throw new EventoInvalidoException("Evento sin idEvento");
        if (e.idAgregado() == null || e.idAgregado().isBlank()) throw new EventoInvalidoException("Evento sin idAgregado");
        if (e.ocurrioEn() == null) throw new EventoInvalidoException("Evento sin fecha (ocurrioEn)");
        if (e.autor() == null || e.autor().isBlank()) throw new EventoInvalidoException("Evento sin autor");
        Map<String, Object> d = e.datos();
        int version = version(d);
        return switch (e.tipo() == null ? "" : e.tipo()) {
            case EventoEntrante.HALLAZGO_REGISTRADO -> creacion(TipoRegistro.HALLAZGO, e, version, estadoHallazgo(d));
            case EventoEntrante.HALLAZGO_MODIFICADO -> modificacionHallazgo(e, version, d);
            case EventoEntrante.FICHA_CREADA -> creacion(TipoRegistro.FICHA, e, version, estadoFicha(d));
            case EventoEntrante.FICHA_MODIFICADA -> modificacionFicha(e, version, d);
            case EventoEntrante.FICHA_DESACTIVADA -> desactivacionFicha(e, version, d);
            default -> throw new EventoInvalidoException("Tipo de evento no soportado: " + e.tipo());
        };
    }

    private static Map<String, Object> estadoHallazgo(Map<String, Object> d) {
        Map<String, Object> estado = new LinkedHashMap<>(d);
        estado.remove("version");
        return estado;
    }

    private static CambioPropuesto modificacionHallazgo(EventoEntrante e, int version, Map<String, Object> d) {
        String campo = texto(d.get("campo"));
        if (campo == null) throw new EventoInvalidoException("Modificación sin el campo modificado");
        String nuevo = texto(d.get("valorNuevo"));
        boolean reclasificacion = "especie".equals(campo);
        return new CambioPropuesto(TipoRegistro.HALLAZGO, e.idAgregado(), version,
                reclasificacion ? TipoCambio.RECLASIFICACION : TipoCambio.MODIFICACION,
                campo, texto(d.get("valorAnterior")), nuevo, texto(d.get("motivo")),
                e.autor(), e.autorizador(), e.ocurrioEn(), null,
                estado -> {
                    estado.put(campo, nuevo);
                    if (reclasificacion) estado.remove("especieId");
                    return estado;
                });
    }

    private static Map<String, Object> estadoFicha(Map<String, Object> d) {
        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("especieId", d.get("especieId"));
        estado.put("atributos", d.get("atributos") instanceof Map<?, ?> m ? new LinkedHashMap<>(m) : new LinkedHashMap<>());
        estado.put("activa", true);
        return estado;
    }

    private static CambioPropuesto modificacionFicha(EventoEntrante e, int version, Map<String, Object> d) {
        String atributo = texto(d.get("campo"));
        if (atributo == null) throw new EventoInvalidoException("Modificación de ficha sin el atributo modificado");
        String nuevo = texto(d.get("valorNuevo"));
        return new CambioPropuesto(TipoRegistro.FICHA, e.idAgregado(), version, TipoCambio.MODIFICACION,
                atributo, texto(d.get("valorAnterior")), nuevo, null,
                e.autor(), e.autorizador(), e.ocurrioEn(), null,
                estado -> {
                    Map<String, Object> atributos = estado.get("atributos") instanceof Map<?, ?> m
                            ? copiar(m) : new LinkedHashMap<>();
                    atributos.put(atributo, nuevo);
                    estado.put("atributos", atributos);
                    return estado;
                });
    }

    private static CambioPropuesto desactivacionFicha(EventoEntrante e, int version, Map<String, Object> d) {
        String motivo = texto(d.get("motivo"));
        return new CambioPropuesto(TipoRegistro.FICHA, e.idAgregado(), version, TipoCambio.DESACTIVACION,
                "activa", "true", "false", motivo,
                e.autor(), e.autorizador(), e.ocurrioEn(), null,
                estado -> {
                    estado.put("activa", false);
                    estado.put("motivoDesactivacion", motivo);
                    return estado;
                });
    }

    private static CambioPropuesto creacion(TipoRegistro tipo, EventoEntrante e, int version, Map<String, Object> estado) {
        return new CambioPropuesto(tipo, e.idAgregado(), version, TipoCambio.CREACION, null, null, null, null,
                e.autor(), e.autorizador(), e.ocurrioEn(), estado, s -> s);
    }

    private static int version(Map<String, Object> d) {
        Object v = d.get("version");
        try {
            if (v instanceof Number n) return n.intValue();
            if (v != null) return Integer.parseInt(v.toString());
        } catch (NumberFormatException ex) {
            throw new EventoInvalidoException("Versión inválida: " + v, ex);
        }
        throw new EventoInvalidoException("Evento sin la versión del registro (datos.version)");
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private static Map<String, Object> copiar(Map<?, ?> origen) {
        Map<String, Object> copia = new LinkedHashMap<>();
        origen.forEach((k, v) -> copia.put(String.valueOf(k), v));
        return copia;
    }
}
