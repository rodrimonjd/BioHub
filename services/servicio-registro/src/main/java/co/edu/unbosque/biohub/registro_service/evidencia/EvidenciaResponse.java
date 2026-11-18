package co.edu.unbosque.biohub.registro_service.evidencia;

import java.util.UUID;

public record EvidenciaResponse(
        UUID id,
        TipoEvidencia tipo,
        String valor,
        String unidad,
        String urlArchivo,
        String atributoTaxonomico) {

    public static EvidenciaResponse desde(Evidencia e) {
        return new EvidenciaResponse(e.getId(), e.getTipo(), e.getValor(), e.getUnidad(),
                e.getUrlArchivo(), e.getAtributoTaxonomico());
    }
}