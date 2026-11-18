package co.edu.unbosque.biohub.registro_service.hallazgo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.biohub.registro_service.evidencia.EvidenciaResponse;

public record HallazgoResponse(
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
        int versionActual,
        Instant creadoEn,
        List<EvidenciaResponse> evidencias) {

    public static HallazgoResponse desde(Hallazgo h) {
        List<EvidenciaResponse> evidencias = h.getEvidencias().stream()
                .map(EvidenciaResponse::desde)
                .toList();
        return new HallazgoResponse(h.getId(), h.getInvestigadorId(),
                h.getEspecie().getId(), h.getEspecie().getNombreCientifico(),
                h.getFichaId(), h.getFecha(), h.getDescripcion(),
                h.getLatitud(), h.getLongitud(), h.getDepartamento(), h.getRegion(),
                h.getVersionActual(), h.getCreadoEn(), evidencias);
    }
}