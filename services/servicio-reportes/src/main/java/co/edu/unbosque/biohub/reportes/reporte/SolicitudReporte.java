package co.edu.unbosque.biohub.reportes.reporte;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * @param solicitadoPor quién pide el reporte; lo fija el controlador con la cabecera X-Usuario-Id
 */
public record SolicitudReporte(
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFin,
        String departamento,
        String especieId,
        String solicitadoPor) {

    public SolicitudReporte conSolicitante(String usuarioId) {
        return new SolicitudReporte(fechaInicio, fechaFin, departamento, especieId, usuarioId);
    }
}
