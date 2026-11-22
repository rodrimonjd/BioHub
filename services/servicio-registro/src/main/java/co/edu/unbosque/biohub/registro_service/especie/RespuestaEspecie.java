package co.edu.unbosque.biohub.registro_service.especie;

import java.util.UUID;

public record RespuestaEspecie(
        UUID id,
        String nombreCientifico,
        String nombreComun,
        String estadoConservacion) {

    public static RespuestaEspecie desde(Especie e) {
        return new RespuestaEspecie(e.getId(), e.getNombreCientifico(), e.getNombreComun(), e.getEstadoConservacion());
    }
}