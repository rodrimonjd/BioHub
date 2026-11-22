package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.time.Instant;
import java.util.UUID;

public record RespuestaAvistamiento(
        UUID id,
        UUID especieId,
        String especie,
        Instant fechaHora,
        int cantidad,
        Double latitud,
        Double longitud,
        String departamento,
        String region,
        Instant creadoEn) {

    public static RespuestaAvistamiento desde(Avistamiento a) {
        return new RespuestaAvistamiento(
                a.getId(),
                a.getEspecie().getId(),
                a.getEspecie().getNombreCientifico(),
                a.getFechaHora(),
                a.getCantidad(),
                a.getLatitud(),
                a.getLongitud(),
                a.getDepartamento(),
                a.getRegion(),
                a.getCreadoEn());
    }
}