package co.edu.unbosque.biohub.autorizacion.compartido;

import co.edu.unbosque.biohub.autorizacion.permiso.Permiso;
import co.edu.unbosque.biohub.autorizacion.rol.Rol;
import co.edu.unbosque.biohub.autorizacion.visibilidad.NivelVisibilidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.TreeSet;

public final class Dtos {
    private Dtos() {}

    public record SolicitudVerificacion(@NotBlank(message = "es obligatorio") String usuarioId,
                                        @NotBlank(message = "es obligatorio") String permiso) {}
    public record RespuestaVerificacion(String usuarioId, String permiso, boolean permitido) {}

    public record RespuestaAccesoUsuario(String usuarioId, Set<String> roles, Set<String> permisos) {}

    public record SolicitudVisibilidad(String usuarioId, String categoriaConservacion) {}
    public record RespuestaVisibilidad(NivelVisibilidad nivel, boolean coordenadasExactas) {}

    public record SolicitudCrearRol(@NotBlank(message = "es obligatorio") @Size(max = 60, message = "máximo 60 caracteres") String nombre,
                                    @Size(max = 255, message = "máximo 255 caracteres") String descripcion,
                                    Set<String> permisos) {}
    public record SolicitudPermisos(Set<String> permisos) {}

    public record VistaRol(String nombre, String descripcion, Set<String> permisos) {
        public static VistaRol desde(Rol r) {
            Set<String> codigos = new TreeSet<>();
            for (Permiso p : r.getPermisos()) codigos.add(p.getCodigo());
            return new VistaRol(r.getNombre(), r.getDescripcion(), codigos);
        }
    }

    public record RespuestaError(int estado, String error, String mensaje) {}
}
