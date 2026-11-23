package co.edu.unbosque.biohub.historial.evento;

import java.time.Instant;
import java.util.Map;

/**
 * Evento de dominio tal como lo publican servicio-registro y servicio-taxonomia (EventoDominio).
 * {@code datos.version} es la versión del registro; el campo {@code version} del sobre es la del esquema.
 */
public record EventoEntrante(String idEvento, String tipo, String idAgregado, Instant ocurrioEn,
                             String autor, String autorizador, Map<String, Object> datos) {

    public static final String HALLAZGO_REGISTRADO = "HALLAZGO_REGISTRADO";
    public static final String HALLAZGO_MODIFICADO = "HALLAZGO_MODIFICADO";
    public static final String FICHA_CREADA = "FICHA_CREADA";
    public static final String FICHA_MODIFICADA = "FICHA_MODIFICADA";
    public static final String FICHA_DESACTIVADA = "FICHA_DESACTIVADA";

    /** Tipos de evento que conserva el historial (son las routing keys a las que se suscribe la cola). */
    public static final String[] TIPOS_ATENDIDOS = {
            HALLAZGO_REGISTRADO, HALLAZGO_MODIFICADO, FICHA_CREADA, FICHA_MODIFICADA, FICHA_DESACTIVADA};

    public Map<String, Object> datos() {
        return datos == null ? Map.of() : datos;
    }
}
