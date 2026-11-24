package co.edu.unbosque.biohub.autorizacion.compartido;

import co.edu.unbosque.biohub.autorizacion.permiso.Permiso;
import co.edu.unbosque.biohub.autorizacion.rol.Rol;
import co.edu.unbosque.biohub.autorizacion.visibilidad.NivelVisibilidad;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

public final class Dtos {
    private Dtos() {}

    public record SolicitudVerificacion(@NotBlank(message = "es obligatorio") String usuarioId,
                                        @NotBlank(message = "es obligatorio") String permiso) {}
    public record RespuestaVerificacion(String usuarioId, String permiso, boolean permitido) {}

    public record RespuestaAccesoUsuario(String usuarioId, Set<String> roles, Set<String> permisos) {}

    public record SolicitudConsumidor(@NotBlank(message = "es obligatorio") String consumidorId) {}
    public record RespuestaConsumidor(String consumidorId, boolean valido, String token) {}

    public record SolicitudVisibilidad(String usuarioId, String categoriaConservacion, String especieId) {}
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

    public record SolicitudSesion(@NotBlank(message = "es obligatorio") String usuario,
                                  @NotBlank(message = "es obligatoria") String clave) {}
    public record RespuestaSesion(String usuarioId, String nombre, String correo,
                                  Set<String> roles, Set<String> permisos) {}

    public record RespuestaInicioSesion(String usuarioId, String nombre, String correo, Set<String> roles,
                                        Set<String> permisos, String token, Instant venceEn) {}

    public record SolicitudCrearUsuario(
            @NotBlank(message = "es obligatorio")
            @Pattern(regexp = "[a-zA-Z0-9._-]{3,100}", message = "solo letras, números, punto, guion o guion bajo (3 a 100)")
            String id,
            @NotBlank(message = "es obligatorio") @Size(max = 150, message = "máximo 150 caracteres") String nombre,
            @NotBlank(message = "es obligatorio") @Email(message = "no es un correo válido") String correo,
            @NotBlank(message = "es obligatoria") @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
            String clave,
            String rol) {}
    public record VistaUsuario(String id, String nombre, String correo, boolean activo, Set<String> roles) {}
    public record ResumenUsuario(String id, String nombre) {}

    public record RespuestaError(int estado, String error, String mensaje) {}
}
