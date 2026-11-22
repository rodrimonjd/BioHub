package co.edu.unbosque.biohub.taxonomia.evento;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Misma estructura de evento que usa servicio-registro, para que los consumidores
 * procesen todos los eventos de BioHub de la misma forma.
 */
public record EventoDominio(
        UUID idEvento,
        String tipo,
        int version,
        String idAgregado,
        Instant ocurrioEn,
        String autor,
        String autorizador,
        Map<String, Object> datos) {

    public static final String FICHA_CREADA = "FICHA_CREADA";
    public static final String FICHA_MODIFICADA = "FICHA_MODIFICADA";
    public static final String FICHA_DESACTIVADA = "FICHA_DESACTIVADA";
}
