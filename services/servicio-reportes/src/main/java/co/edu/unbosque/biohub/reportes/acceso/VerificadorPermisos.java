package co.edu.unbosque.biohub.reportes.acceso;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class VerificadorPermisos {

    public static final String CABECERA_USUARIO = "X-Usuario-Id";

    public static final String VER_REPORTES = "reporte:ver";
    public static final String GENERAR_REPORTES = "reporte:generar";

    private final ClienteAutorizacion autorizacion;

    public VerificadorPermisos(ClienteAutorizacion autorizacion) {
        this.autorizacion = autorizacion;
    }

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
}
