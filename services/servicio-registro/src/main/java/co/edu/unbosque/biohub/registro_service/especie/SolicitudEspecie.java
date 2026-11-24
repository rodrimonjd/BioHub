package co.edu.unbosque.biohub.registro_service.especie;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SolicitudEspecie(
        @NotBlank @Size(max = 150) String nombreCientifico,
        @Size(max = 150) String nombreComun,
        @NotBlank @Pattern(regexp = "LC|NT|VU|EN|CR", message = "debe ser LC, NT, VU, EN o CR")
        String estadoConservacion) {
}
