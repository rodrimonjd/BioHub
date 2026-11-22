package co.edu.unbosque.biohub.registro_service.equipo;

import jakarta.validation.constraints.NotBlank;

public record EquipoRequest(
        @NotBlank String numeroSerie,
        @NotBlank String tipo,
        String modelo) {
}