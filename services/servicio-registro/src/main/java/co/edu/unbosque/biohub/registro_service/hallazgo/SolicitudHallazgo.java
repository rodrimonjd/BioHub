package co.edu.unbosque.biohub.registro_service.hallazgo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.biohub.registro_service.evidencia.SolicitudEvidencia;

public record SolicitudHallazgo(
        String investigadorId,
        @NotNull UUID especieId,
        String fichaId,
        @NotNull @PastOrPresent Instant fecha,
        @NotBlank @Size(max = 1000) String descripcion,
        @DecimalMin("-90") @DecimalMax("90") Double latitud,
        @DecimalMin("-180") @DecimalMax("180") Double longitud,
        @NotBlank String departamento,
        String region,
        UUID expedicionId,
        @Valid List<SolicitudEvidencia> evidencias) {

    public SolicitudHallazgo conInvestigador(String usuarioId) {
        return new SolicitudHallazgo(usuarioId, especieId, fichaId, fecha, descripcion, latitud, longitud,
                departamento, region, expedicionId, evidencias);
    }
}
