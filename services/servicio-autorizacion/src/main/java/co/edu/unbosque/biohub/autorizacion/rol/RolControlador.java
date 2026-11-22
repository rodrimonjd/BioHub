package co.edu.unbosque.biohub.autorizacion.rol;

import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Administración de roles. El Gateway autentica y reenvía el id del usuario
 * en la cabecera X-Usuario-Id; aquí se exige el permiso roles:administrar.
 */
@RestController
@RequestMapping("/api/v1/autorizacion")
public class RolControlador {

    private static final String CABECERA_USUARIO = "X-Usuario-Id";
    private final RolServicio servicio;

    public RolControlador(RolServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/roles")
    public List<VistaRol> listar(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante) {
        servicio.exigirAdmin(solicitante);
        return servicio.listarRoles().stream().map(VistaRol::desde).toList();
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public VistaRol crear(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                          @Valid @RequestBody SolicitudCrearRol solicitud) {
        servicio.exigirAdmin(solicitante);
        return VistaRol.desde(servicio.crearRol(solicitud.nombre(), solicitud.descripcion(), solicitud.permisos()));
    }

    @PutMapping("/roles/{nombre}/permisos")
    public VistaRol definirPermisos(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                                    @PathVariable String nombre,
                                    @RequestBody SolicitudPermisos solicitud) {
        servicio.exigirAdmin(solicitante);
        return VistaRol.desde(servicio.definirPermisos(nombre, solicitud.permisos()));
    }

    @PostMapping("/usuarios/{usuarioId}/roles/{nombreRol}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void asignar(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                        @PathVariable String usuarioId, @PathVariable String nombreRol) {
        servicio.exigirAdmin(solicitante);
        servicio.asignarRol(usuarioId, nombreRol);
    }

    @DeleteMapping("/usuarios/{usuarioId}/roles/{nombreRol}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revocar(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                        @PathVariable String usuarioId, @PathVariable String nombreRol) {
        servicio.exigirAdmin(solicitante);
        servicio.revocarRol(usuarioId, nombreRol);
    }
}
