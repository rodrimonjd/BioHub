package co.edu.unbosque.biohub.autorizacion.autorizacion;

import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.*;
import co.edu.unbosque.biohub.autorizacion.visibilidad.NivelVisibilidad;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/autorizacion")
public class AutorizacionControlador {

    private final ServicioAutorizacion autorizacion;

    public AutorizacionControlador(ServicioAutorizacion autorizacion) {
        this.autorizacion = autorizacion;
    }

    @PostMapping("/verificar")
    public RespuestaVerificacion verificar(@Valid @RequestBody SolicitudVerificacion solicitud) {
        boolean permitido = autorizacion.verificar(solicitud.usuarioId(), solicitud.permiso());
        return new RespuestaVerificacion(solicitud.usuarioId(), solicitud.permiso(), permitido);
    }

    @GetMapping("/usuarios/{usuarioId}/permisos")
    public RespuestaAccesoUsuario acceso(@PathVariable String usuarioId) {
        return new RespuestaAccesoUsuario(usuarioId, autorizacion.rolesDe(usuarioId), autorizacion.permisosDe(usuarioId));
    }

    @PostMapping("/visibilidad-ubicacion")
    public RespuestaVisibilidad visibilidad(@RequestBody SolicitudVisibilidad solicitud) {
        NivelVisibilidad nivel = autorizacion.visibilidadUbicacion(solicitud.usuarioId(), solicitud.categoriaConservacion());
        return new RespuestaVisibilidad(nivel, nivel == NivelVisibilidad.EXACTA);
    }
}
