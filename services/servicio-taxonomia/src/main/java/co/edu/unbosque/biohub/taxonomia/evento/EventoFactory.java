package co.edu.unbosque.biohub.taxonomia.evento;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import co.edu.unbosque.biohub.taxonomia.ficha.FichaTaxonomica;
import co.edu.unbosque.biohub.taxonomia.ficha.ValorAtributo;

/**
 * Patron Factory: crea los eventos de las fichas, todos con la misma estructura.
 */
public final class EventoFactory {

    private EventoFactory() { }

    public static EventoDominio fichaCreada(FichaTaxonomica f) {
        Map<String, Object> valores = new LinkedHashMap<>();
        for (ValorAtributo v : f.getAtributos()) {
            valores.put(v.getAtributo().getNombre(), v.getValor());
        }
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("especieId", f.getEspecieId());
        datos.put("version", f.getVersion());
        datos.put("atributos", valores);
        return crear(EventoDominio.FICHA_CREADA, f, f.getCreadoPor(), datos);
    }

    public static EventoDominio fichaModificada(FichaTaxonomica f, String campo,
                                                String valorAnterior, String valorNuevo, String autor) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("especieId", f.getEspecieId());
        datos.put("campo", campo);
        datos.put("valorAnterior", valorAnterior);
        datos.put("valorNuevo", valorNuevo);
        datos.put("version", f.getVersion());
        return crear(EventoDominio.FICHA_MODIFICADA, f, autor, datos);
    }

    public static EventoDominio fichaDesactivada(FichaTaxonomica f, String autor, String motivo) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("especieId", f.getEspecieId());
        datos.put("motivo", motivo);
        datos.put("version", f.getVersion());
        return crear(EventoDominio.FICHA_DESACTIVADA, f, autor, datos);
    }

    private static EventoDominio crear(String tipo, FichaTaxonomica f, String autor,
                                       Map<String, Object> datos) {
        return new EventoDominio(UUID.randomUUID(), tipo, 1, f.getId(), Instant.now(),
                autor, null, datos);
    }
}
