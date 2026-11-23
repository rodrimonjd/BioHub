package co.edu.unbosque.biohub.historial.evento;

import java.time.Instant;
import java.util.Map;

public record EventoEntrante(String idEvento, String tipo, String idAgregado, Instant ocurrioEn,
                             String autor, String autorizador, Map<String, Object> datos) {

    public static final String HALLAZGO_REGISTRADO = "HALLAZGO_REGISTRADO";
    public static final String HALLAZGO_MODIFICADO = "HALLAZGO_MODIFICADO";
    public static final String FICHA_CREADA = "FICHA_CREADA";
    public static final String FICHA_MODIFICADA = "FICHA_MODIFICADA";
    public static final String FICHA_DESACTIVADA = "FICHA_DESACTIVADA";

    public static final String[] TIPOS_ATENDIDOS = {
            HALLAZGO_REGISTRADO, HALLAZGO_MODIFICADO, FICHA_CREADA, FICHA_MODIFICADA, FICHA_DESACTIVADA};

    public Map<String, Object> datos() {
        return datos == null ? Map.of() : datos;
    }
}
