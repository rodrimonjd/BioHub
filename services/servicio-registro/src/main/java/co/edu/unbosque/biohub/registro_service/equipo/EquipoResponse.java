package co.edu.unbosque.biohub.registro_service.equipo;

import java.util.UUID;

public record EquipoResponse(
        UUID id,
        String numeroSerie,
        String tipo,
        String modelo,
        EstadoEquipo estado,
        String responsableId) {

    public static EquipoResponse de(EquipoCampo e) {
        return new EquipoResponse(e.getId(), e.getNumeroSerie(), e.getTipo(), e.getModelo(), e.getEstado(), e.getResponsableId());
    }
}