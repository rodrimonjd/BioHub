package co.edu.unbosque.biohub.registro_service.equipo;

import jakarta.validation.constraints.NotBlank;

public record SolicitudResponsable(@NotBlank String investigadorId) {
}