package co.edu.unbosque.biohub.registro_service.hallazgo;

import jakarta.validation.constraints.NotBlank;

/** autor se toma de la cabecera X-Usuario-Id: el valor que venga en el cuerpo se ignora. */
public record SolicitudModificarHallazgo(
        @NotBlank String campo,
        @NotBlank String valor,
        String autor) {

    public SolicitudModificarHallazgo conAutor(String usuarioId) {
        return new SolicitudModificarHallazgo(campo, valor, usuarioId);
    }
}
