package co.edu.unbosque.biohub.registro_service.hallazgo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReclasificarHallazgoRequest(
        @NotNull UUID nuevaEspecieId,
        @NotBlank @Size(max = 500) String motivo,
        @NotBlank String autor,
        @NotBlank String autorizador) { }