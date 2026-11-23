package co.edu.unbosque.biohub.historial.permiso;

import co.edu.unbosque.biohub.historial.compartido.ProhibidoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VerificadorPermisos {

    private final ClienteAutorizacion autorizacion;
    private final String permiso;

    public VerificadorPermisos(ClienteAutorizacion autorizacion,
                               @Value("${historial.permiso-consulta}") String permiso) {
        this.autorizacion = autorizacion;
        this.permiso = permiso;
    }

    public void exigir(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ProhibidoException("Se requiere la cabecera X-Usuario-Id con el permiso " + permiso);
        }
        if (!autorizacion.tienePermiso(usuarioId.trim(), permiso)) {
            throw new ProhibidoException("Se requiere el permiso " + permiso);
        }
    }
}
