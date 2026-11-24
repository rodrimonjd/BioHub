package co.edu.unbosque.biohub.historial.permiso;

import co.edu.unbosque.biohub.historial.compartido.ProhibidoException;
import co.edu.unbosque.biohub.historial.configuracion.PropiedadesHistorial;
import org.springframework.stereotype.Component;

@Component
public class VerificadorPermisos {

    private final ClienteAutorizacion autorizacion;
    private final String permiso;

    public VerificadorPermisos(ClienteAutorizacion autorizacion, PropiedadesHistorial propiedades) {
        this.autorizacion = autorizacion;
        this.permiso = propiedades.permisoConsulta();
    }

    public void exigir(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ProhibidoException("Se requiere iniciar sesión con el permiso " + permiso);
        }
        if (!autorizacion.tienePermiso(usuarioId.trim(), permiso)) {
            throw new ProhibidoException("Se requiere el permiso " + permiso);
        }
    }
}
