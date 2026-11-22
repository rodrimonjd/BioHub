package co.edu.unbosque.biohub.consulta.evento;

import java.util.Map;

/**
 * Evento de dominio tal como lo publica servicio-registro (EventoDominio).
 * Solo se leen los campos que necesita el modelo de lectura.
 */
public record EventoEntrante(String idEvento, String tipo, String idAgregado, Map<String, Object> datos) {

    public static final String HALLAZGO_REGISTRADO = "HALLAZGO_REGISTRADO";
    public static final String HALLAZGO_MODIFICADO = "HALLAZGO_MODIFICADO";

    public Map<String, Object> datos() {
        return datos == null ? Map.of() : datos;
    }
}
