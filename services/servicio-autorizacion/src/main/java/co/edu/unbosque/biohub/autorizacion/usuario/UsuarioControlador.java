package co.edu.unbosque.biohub.autorizacion.usuario;

import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.ResumenUsuario;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.RespuestaSesion;
import co.edu.unbosque.biohub.autorizacion.compartido.ProhibidoException;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.SolicitudCrearUsuario;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.SolicitudSesion;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.VistaUsuario;
import co.edu.unbosque.biohub.autorizacion.rol.RolServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Inicio de sesión y administración de cuentas. */
@RestController
@RequestMapping("/api/v1/autorizacion")
public class UsuarioControlador {

    private static final String CABECERA_USUARIO = "X-Usuario-Id";

    private final ServicioUsuarios servicio;
    private final RolServicio roles;

    public UsuarioControlador(ServicioUsuarios servicio, RolServicio roles) {
        this.servicio = servicio;
        this.roles = roles;
    }

    /** Usuario (id o correo) y contraseña → datos de la cuenta con sus roles y permisos. 401 si no coinciden. */
    @PostMapping("/sesiones")
    public RespuestaSesion iniciarSesion(@Valid @RequestBody SolicitudSesion solicitud) {
        return servicio.iniciarSesion(solicitud.usuario(), solicitud.clave());
    }

    @GetMapping("/usuarios")
    public List<VistaUsuario> listar(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante) {
        roles.exigirAdmin(solicitante);
        return servicio.listar();
    }

    /** Cuentas que tienen un permiso, p. ej. hallazgo:validar para elegir quién autoriza una reclasificación. */
    @GetMapping("/usuarios/con-permiso/{permiso}")
    public List<ResumenUsuario> conPermiso(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                                           @PathVariable String permiso) {
        if (!servicio.esCuentaActiva(solicitante)) {
            throw new ProhibidoException("Se requiere iniciar sesión con una cuenta activa");
        }
        return servicio.conPermiso(permiso);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public VistaUsuario crear(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                              @Valid @RequestBody SolicitudCrearUsuario solicitud) {
        roles.exigirAdmin(solicitante);
        return servicio.crear(solicitud);
    }
}
