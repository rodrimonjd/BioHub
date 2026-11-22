package co.edu.unbosque.biohub.registro_service.evento;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventoDominio(
        UUID idEvento,
        String tipo,
        int version,
        String idAgregado,
        Instant ocurrioEn,
        String autor,
        String autorizador,
        Map<String, Object> datos) {

    public static final String HALLAZGO_REGISTRADO = "HALLAZGO_REGISTRADO";
    public static final String HALLAZGO_MODIFICADO = "HALLAZGO_MODIFICADO";
    public static final String AVISTAMIENTO_REGISTRADO = "AVISTAMIENTO_REGISTRADO";
    public static final String EQUIPO_ASIGNADO = "EQUIPO_ASIGNADO";
}