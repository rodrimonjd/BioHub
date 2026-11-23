package co.edu.unbosque.biohub.taxonomia.ficha;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class SolicitudesFicha {

    private SolicitudesFicha() { }

    public record SolicitudValor(
            @NotBlank String nombre,
            String valor) { }

    public record CrearFicha(
            @NotNull UUID especieId,
            String autor,
            @NotEmpty List<@Valid SolicitudValor> atributos) {

        public CrearFicha conAutor(String usuarioId) {
            return new CrearFicha(especieId, usuarioId, atributos);
        }
    }

    public record ModificarValor(
            String valor,
            String autor) {

        public ModificarValor conAutor(String usuarioId) {
            return new ModificarValor(valor, usuarioId);
        }
    }

    public record IncorporarAtributo(
            @NotBlank String nombre,
            String valor,
            String autor) {

        public IncorporarAtributo conAutor(String usuarioId) {
            return new IncorporarAtributo(nombre, valor, usuarioId);
        }
    }

    public record DesactivarFicha(
            String autor,
            @Size(max = 500) String motivo) {

        public DesactivarFicha conAutor(String usuarioId) {
            return new DesactivarFicha(usuarioId, motivo);
        }
    }
}
