package co.edu.unbosque.biohub.autorizacion.ubicacion;

import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.ResumenUsuario;
import co.edu.unbosque.biohub.autorizacion.ubicacion.ServicioAccesoUbicacion.AccesoDeUsuario;
import co.edu.unbosque.biohub.autorizacion.ubicacion.ServicioAccesoUbicacion.VistaAcceso;
import co.edu.unbosque.biohub.autorizacion.ubicacion.ServicioAccesoUbicacion.VistaCambio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/autorizacion")
public class AccesoUbicacionControlador {

    private static final String CABECERA_USUARIO = "X-Usuario-Id";

    private final ServicioAccesoUbicacion servicio;

    public AccesoUbicacionControlador(ServicioAccesoUbicacion servicio) {
        this.servicio = servicio;
    }

    public record SolicitudAcceso(@NotBlank(message = "es obligatorio") String usuarioId,
                                  @NotBlank(message = "es obligatorio") String especieId) {}

    @GetMapping("/usuarios/{usuarioId}/ubicacion")
    public AccesoDeUsuario acceso(@PathVariable String usuarioId) {
        return servicio.accesoDe(usuarioId);
    }

    @GetMapping("/ubicacion/accesos")
    public List<VistaAcceso> listar(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante) {
        return servicio.listar(solicitante);
    }

    @PostMapping("/ubicacion/accesos")
    @ResponseStatus(HttpStatus.CREATED)
    public VistaAcceso otorgar(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                               @Valid @RequestBody SolicitudAcceso solicitud) {
        return servicio.otorgar(solicitante, solicitud.usuarioId(), solicitud.especieId());
    }

    @DeleteMapping("/ubicacion/accesos/{usuarioId}/{especieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revocar(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante,
                        @PathVariable String usuarioId, @PathVariable String especieId) {
        servicio.revocar(solicitante, usuarioId, especieId);
    }

    @GetMapping("/ubicacion/historial")
    public List<VistaCambio> historial(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante) {
        return servicio.historial(solicitante);
    }

    @GetMapping("/ubicacion/usuarios")
    public List<ResumenUsuario> usuarios(@RequestHeader(value = CABECERA_USUARIO, required = false) String solicitante) {
        return servicio.usuarios(solicitante);
    }
}
