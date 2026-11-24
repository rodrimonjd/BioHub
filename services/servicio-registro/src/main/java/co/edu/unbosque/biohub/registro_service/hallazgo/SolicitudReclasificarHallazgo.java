package co.edu.unbosque.biohub.registro_service.hallazgo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * autor se toma de la cabecera X-Usuario-Id. El autorizador debe tener el permiso hallazgo:validar
 * (lo verifica el controlador con servicio-autorizacion).
 */
public record SolicitudReclasificarHallazgo(
        @NotNull UUID nuevaEspecieId,
        @NotBlank @Size(max = 500) String motivo,
        String autor,
        @NotBlank String autorizador) {

    public SolicitudReclasificarHallazgo conAutor(String usuarioId) {
        return new SolicitudReclasificarHallazgo(nuevaEspecieId, motivo, usuarioId, autorizador);
    }
}
