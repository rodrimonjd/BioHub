package co.edu.unbosque.biohub.registro_service.evidencia;

import java.util.UUID;

public record RespuestaEvidencia(
        UUID id,
        TipoEvidencia tipo,
        String valor,
        String unidad,
        String urlArchivo,
        String atributoTaxonomico) {

    public static RespuestaEvidencia desde(Evidencia e) {
        return new RespuestaEvidencia(e.getId(), e.getTipo(), e.getValor(), e.getUnidad(), e.getUrlArchivo(), e.getAtributoTaxonomico());
    }
}