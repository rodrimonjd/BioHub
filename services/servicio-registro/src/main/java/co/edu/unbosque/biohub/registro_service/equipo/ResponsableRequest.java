package co.edu.unbosque.biohub.registro_service.equipo;

import jakarta.validation.constraints.NotBlank;

public record ResponsableRequest(@NotBlank String investigadorId) {
}