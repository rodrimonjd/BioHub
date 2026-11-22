package co.edu.unbosque.biohub.registro_service.equipo;

import java.util.UUID;

public record RespuestaEquipo(
        UUID id,
        String numeroSerie,
        String tipo,
        String modelo,
        EstadoEquipo estado,
        String responsableId) {

    public static RespuestaEquipo de(EquipoCampo e) {
        return new RespuestaEquipo(e.getId(), e.getNumeroSerie(), e.getTipo(), e.getModelo(), e.getEstado(), e.getResponsableId());
    }
}