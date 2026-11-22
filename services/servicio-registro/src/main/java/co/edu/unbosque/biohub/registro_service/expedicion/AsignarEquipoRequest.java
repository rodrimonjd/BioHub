package co.edu.unbosque.biohub.registro_service.expedicion;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AsignarEquipoRequest(@NotNull UUID equipoId) {
}