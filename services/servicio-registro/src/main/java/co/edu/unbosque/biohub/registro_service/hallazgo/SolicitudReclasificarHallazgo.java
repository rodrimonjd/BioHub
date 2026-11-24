package co.edu.unbosque.biohub.registro_service.hallazgo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SolicitudReclasificarHallazgo(
        @NotNull UUID nuevaEspecieId,
        @NotBlank @Size(max = 500) String motivo,
        String autor,
        @NotBlank String autorizador) {

    public SolicitudReclasificarHallazgo conAutor(String usuarioId) {
        return new SolicitudReclasificarHallazgo(nuevaEspecieId, motivo, usuarioId, autorizador);
    }
}
