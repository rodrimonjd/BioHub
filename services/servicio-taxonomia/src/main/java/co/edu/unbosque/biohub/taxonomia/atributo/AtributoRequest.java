package co.edu.unbosque.biohub.taxonomia.atributo;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * obligatorio es Boolean (no boolean) para que se pueda omitir en el JSON; si falta, es false.
 */
public record AtributoRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotNull TipoDato tipoDato,
        String unidad,
        List<String> opciones,
        Boolean obligatorio) { }
