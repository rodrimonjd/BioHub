package co.edu.unbosque.biohub.registro_service.especie;

import java.util.UUID;

public record EspecieResponse(
        UUID id,
        String nombreCientifico,
        String nombreComun,
        String estadoConservacion) {

    public static EspecieResponse desde(Especie e) {
        return new EspecieResponse(e.getId(), e.getNombreCientifico(),
                e.getNombreComun(), e.getEstadoConservacion());
    }
}