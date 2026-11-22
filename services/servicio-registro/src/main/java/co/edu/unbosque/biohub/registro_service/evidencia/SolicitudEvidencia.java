package co.edu.unbosque.biohub.registro_service.evidencia;

import jakarta.validation.constraints.NotNull;

public record SolicitudEvidencia(
        @NotNull TipoEvidencia tipo,
        String valor,
        String unidad,
        String urlArchivo,
        String atributoTaxonomico) { }