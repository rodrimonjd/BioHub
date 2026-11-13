package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.time.Instant;
import java.util.UUID;

public record AvistamientoResponse(UUID id, UUID especieId, Instant fechaHora, int cantidad,
        Double latitud, Double longitud, String departamento, String region, Instant creadoEn) {

    public static AvistamientoResponse desde(Avistamiento a) {
        return new AvistamientoResponse(a.getId(), a.getEspecie().getId(), a.getFechaHora(),
                a.getCantidad(), a.getLatitud(), a.getLongitud(),
                a.getDepartamento(), a.getRegion(), a.getCreadoEn());
    }
}