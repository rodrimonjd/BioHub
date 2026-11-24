package co.edu.unbosque.biohub.registro_service.acceso;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exige permisos de servicio-autorizacion antes de cada escritura (ASR-05: permisos centralizados).
 * El usuario llega en la cabecera X-Usuario-Id; los ids de autor/investigador del cuerpo no se usan.
 */
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

    /** @return el id del usuario (sin espacios), listo para usarse como autor */
    public String exigir(String usuarioId, String permiso) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Se requiere la cabecera " + CABECERA_USUARIO + " con el permiso " + permiso);
        }
        String usuario = usuarioId.trim();
        if (!autorizacion.tienePermiso(usuario, permiso)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el permiso " + permiso);
        }
        return usuario;
    }

    public boolean puedeVerUbicacionExacta(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            return false;
        }
        return autorizacion.tienePermiso(usuarioId.trim(), VER_UBICACION_EXACTA);
    }

    /** La reclasificación necesita un autorizador con permiso hallazgo:validar (HU-10). */
    public void exigirAutorizador(String autorizadorId) {
        if (autorizadorId == null || autorizadorId.isBlank()
                || !autorizacion.tienePermiso(autorizadorId.trim(), VALIDAR_HALLAZGO)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El autorizador indicado no tiene el permiso " + VALIDAR_HALLAZGO);
        }
    }
}
