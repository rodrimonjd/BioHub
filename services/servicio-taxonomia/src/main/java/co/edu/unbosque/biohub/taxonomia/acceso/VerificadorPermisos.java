package co.edu.unbosque.biohub.taxonomia.acceso;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class VerificadorPermisos {

    public static final String CABECERA_USUARIO = "X-Usuario-Id";
    public static final String EDITAR_FICHA = "ficha:editar";

    private final ClienteAutorizacion autorizacion;

    public VerificadorPermisos(ClienteAutorizacion autorizacion) {
        this.autorizacion = autorizacion;
    }

    /** @return el id del usuario (sin espacios), listo para usarse como autor */
    public String exigirEdicion(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Se requiere la cabecera " + CABECERA_USUARIO + " con el permiso " + EDITAR_FICHA);
        }
        String usuario = usuarioId.trim();
        if (!autorizacion.tienePermiso(usuario, EDITAR_FICHA)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el permiso " + EDITAR_FICHA);
        }
        return usuario;
    }
}
