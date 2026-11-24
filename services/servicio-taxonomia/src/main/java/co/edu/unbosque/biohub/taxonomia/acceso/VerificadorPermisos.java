package co.edu.unbosque.biohub.taxonomia.acceso;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class VerificadorPermisos {

    public static final String CABECERA_USUARIO = "X-Usuario-Id";
    public static final String EDITAR_FICHA = "ficha:editar";
    public static final String DESACTIVAR_FICHA = "ficha:desactivar";

    private final ClienteAutorizacion autorizacion;

    public VerificadorPermisos(ClienteAutorizacion autorizacion) {
        this.autorizacion = autorizacion;
    }

    /** Crear fichas, agregar atributos y cambiar valores. @return el id del usuario, listo para usarse como autor */
    public String exigirEdicion(String usuarioId) {
        return exigir(usuarioId, EDITAR_FICHA);
    }

    /** Desactivar una ficha. @return el id del usuario, listo para usarse como autor */
    public String exigirDesactivacion(String usuarioId) {
        return exigir(usuarioId, DESACTIVAR_FICHA);
    }

    private String exigir(String usuarioId, String permiso) {
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
}
