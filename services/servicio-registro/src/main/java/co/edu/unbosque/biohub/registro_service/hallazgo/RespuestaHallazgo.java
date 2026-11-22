package co.edu.unbosque.biohub.registro_service.hallazgo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.biohub.registro_service.evidencia.RespuestaEvidencia;

public record RespuestaHallazgo(
        UUID id,
        String investigadorId,
        UUID especieId,
        String especie,
        String fichaId,
        Instant fecha,
        String descripcion,
        Double latitud,
        Double longitud,
        String departamento,
        String region,
        UUID expedicionId,
        int versionActual,
        Instant creadoEn,
        List<RespuestaEvidencia> evidencias) {

    public static RespuestaHallazgo desde(Hallazgo h) {
        UUID expedicionId = null;
        if (h.getExpedicion() != null) {
            expedicionId = h.getExpedicion().getId();
        }
        List<RespuestaEvidencia> evidencias = h.getEvidencias().stream().map(RespuestaEvidencia::desde).toList();
        return new RespuestaHallazgo(h.getId(), h.getInvestigadorId(),
                h.getEspecie().getId(), h.getEspecie().getNombreCientifico(),
                h.getFichaId(), h.getFecha(), h.getDescripcion(),
                h.getLatitud(), h.getLongitud(), h.getDepartamento(), h.getRegion(),
                expedicionId,
                h.getVersionActual(), h.getCreadoEn(), evidencias);
    }
}