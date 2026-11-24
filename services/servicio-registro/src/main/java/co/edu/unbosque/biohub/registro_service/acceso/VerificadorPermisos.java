package co.edu.unbosque.biohub.registro_service.acceso;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Component
public class VerificadorPermisos {

    public static final String CABECERA_USUARIO = "X-Usuario-Id";

    public static final String CREAR_HALLAZGO = "hallazgo:crear";
    public static final String EDITAR_HALLAZGO = "hallazgo:editar";
    public static final String VALIDAR_HALLAZGO = "hallazgo:validar";
    public static final String GESTIONAR_EQUIPOS = "equipo:gestionar";
    public static final String VER_UBICACION_EXACTA = "ubicacion:ver_exacta";

    private final ClienteAutorizacion autorizacion;

    public VerificadorPermisos(ClienteAutorizacion autorizacion) {
        this.autorizacion = autorizacion;
    }

    public String exigir(String usuarioId, String permiso) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Se requiere iniciar sesión con el permiso " + permiso);
        }
        String usuario = usuarioId.trim();
        if (!autorizacion.tienePermiso(usuario, permiso)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el permiso " + permiso);
        }
        return usuario;
    }

    public Predicate<UUID> ubicacionExacta(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            return especie -> false;
        }
        String usuario = usuarioId.trim();
        if (autorizacion.tienePermiso(usuario, VER_UBICACION_EXACTA)) {
            return especie -> true;
        }
        Set<String> especies = autorizacion.especiesConUbicacionExacta(usuario);
        return especie -> especie != null && especies != null && especies.contains(especie.toString());
    }

    public void exigirAutorizador(String autorizadorId) {
        if (autorizadorId == null || autorizadorId.isBlank()
                || !autorizacion.tienePermiso(autorizadorId.trim(), VALIDAR_HALLAZGO)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El autorizador indicado no tiene el permiso " + VALIDAR_HALLAZGO);
        }
    }
}
