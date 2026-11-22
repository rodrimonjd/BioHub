package co.edu.unbosque.biohub.taxonomia.ficha;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpos de las peticiones de fichas.
 */
public final class FichaRequests {

    private FichaRequests() { }

    /** Atributo del catalogo (por nombre) y su valor, que puede venir vacio si no es obligatorio. */
    public record ValorRequest(
            @NotBlank String nombre,
            String valor) { }

    public record CrearFicha(
            @NotNull UUID especieId,
            @NotBlank String autor,
            @NotEmpty List<@Valid ValorRequest> atributos) { }

    public record ModificarValor(
            String valor,
            @NotBlank String autor) { }

    public record IncorporarAtributo(
            @NotBlank String nombre,
            String valor,
            @NotBlank String autor) { }

    public record DesactivarFicha(
            @NotBlank String autor,
            @Size(max = 500) String motivo) { }
}
