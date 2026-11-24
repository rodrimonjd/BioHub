package co.edu.unbosque.biohub.autorizacion.autorizacion;

import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.*;
import co.edu.unbosque.biohub.autorizacion.compartido.ProhibidoException;
import co.edu.unbosque.biohub.autorizacion.identidad.ServicioTokens;
import co.edu.unbosque.biohub.autorizacion.rol.RolServicio;
import co.edu.unbosque.biohub.autorizacion.ubicacion.ServicioAccesoUbicacion;
import co.edu.unbosque.biohub.autorizacion.usuario.ServicioUsuarios;
import co.edu.unbosque.biohub.autorizacion.visibilidad.NivelVisibilidad;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/autorizacion")
public class AutorizacionControlador {

    private static final String CABECERA_USUARIO = "X-Usuario-Id";
    private static final String CABECERA_SECRETO = "X-Secreto-Servicio";

    private final ServicioAutorizacion autorizacion;
    private final ServicioAccesoUbicacion accesosUbicacion;
    private final ServicioTokens tokens;

    public AutorizacionControlador(ServicioAutorizacion autorizacion, ServicioAccesoUbicacion accesosUbicacion,
                                   ServicioTokens tokens) {
        this.autorizacion = autorizacion;
        this.accesosUbicacion = accesosUbicacion;
        this.tokens = tokens;
    }

    @PostMapping("/verificar")
    public RespuestaVerificacion verificar(@Valid @RequestBody SolicitudVerificacion solicitud) {
        boolean permitido = autorizacion.verificar(solicitud.usuarioId(), solicitud.permiso());
        return new RespuestaVerificacion(solicitud.usuarioId(), solicitud.permiso(), permitido);
    }

    @GetMapping("/usuarios/{usuarioId}/permisos")
    public RespuestaAccesoUsuario acceso(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                                         @PathVariable String usuarioId) {
        boolean propio = usuarioId.equals(solicitante) || ServicioUsuarios.ID_INVITADO.equals(usuarioId);
        if (!propio && !autorizacion.verificar(solicitante, RolServicio.PERMISO_ADMIN)) {
            throw new ProhibidoException("Solo puedes consultar los permisos de tu propia cuenta");
        }
        return new RespuestaAccesoUsuario(usuarioId, autorizacion.rolesDe(usuarioId), autorizacion.permisosDe(usuarioId));
    }

    @PostMapping("/consumidores/validar")
    public RespuestaConsumidor validarConsumidor(@RequestHeader(value = CABECERA_SECRETO, required = false) String secreto,
                                                 @Valid @RequestBody SolicitudConsumidor solicitud) {
        boolean valido = autorizacion.validarConsumidor(solicitud.consumidorId());
        String token = valido && tokens.esServicioConfiable(secreto)
                ? tokens.paraConsumidor(solicitud.consumidorId()).valor()
                : null;
        return new RespuestaConsumidor(solicitud.consumidorId(), valido, token);
    }

    @PostMapping("/visibilidad-ubicacion")
    public RespuestaVisibilidad visibilidad(@RequestBody SolicitudVisibilidad solicitud) {
        NivelVisibilidad nivel = accesosUbicacion.puedeVerEspecie(solicitud.usuarioId(), solicitud.especieId())
                ? NivelVisibilidad.EXACTA
                : autorizacion.visibilidadUbicacion(solicitud.usuarioId(), solicitud.categoriaConservacion());
        return new RespuestaVisibilidad(nivel, nivel == NivelVisibilidad.EXACTA);
    }
}
