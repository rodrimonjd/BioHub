package co.edu.unbosque.biohub.autorizacion.rol;

import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    public record VistaCambioRol(Long id, CambioRol.Accion accion, String usuarioId, String rol, String detalle,
                                 String realizadoPor, java.time.Instant realizadoEn) {}

    @GetMapping("/roles/historial")
    public List<VistaCambioRol> historial(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante) {
        servicio.exigirAdmin(solicitante);
        return servicio.historial().stream()
                .map(c -> new VistaCambioRol(c.getId(), c.getAccion(), c.getUsuarioId(), c.getRol(), c.getDetalle(),
                        c.getRealizadoPor(), c.getRealizadoEn()))
                .toList();
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public VistaRol crear(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                          @Valid @RequestBody SolicitudCrearRol solicitud) {
        servicio.exigirAdmin(solicitante);
        return VistaRol.desde(servicio.crearRol(solicitud.nombre(), solicitud.descripcion(), solicitud.permisos(), solicitante.trim()));
    }

    @PutMapping("/roles/{nombre}/permisos")
    public VistaRol definirPermisos(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                                    @PathVariable String nombre,
                                    @RequestBody SolicitudPermisos solicitud) {
        servicio.exigirAdmin(solicitante);
        return VistaRol.desde(servicio.definirPermisos(nombre, solicitud.permisos(), solicitante.trim()));
    }

    @PostMapping("/usuarios/{usuarioId}/roles/{nombreRol}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void asignar(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                        @PathVariable String usuarioId, @PathVariable String nombreRol) {
        servicio.exigirAdmin(solicitante);
        servicio.asignarRol(usuarioId, nombreRol, solicitante.trim());
    }

    @DeleteMapping("/usuarios/{usuarioId}/roles/{nombreRol}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revocar(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                        @PathVariable String usuarioId, @PathVariable String nombreRol) {
        servicio.exigirAdmin(solicitante);
        servicio.revocarRol(usuarioId, nombreRol, solicitante.trim());
    }
}
