package co.edu.unbosque.biohub.registro_service.hallazgo;

import co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos.CABECERA_USUARIO;

@RestController
@RequestMapping("/hallazgos")
public class HallazgoControlador {

    private final HallazgoServicio service;
    private final VerificadorPermisos permisos;

    public HallazgoControlador(HallazgoServicio service, VerificadorPermisos permisos) {
        this.service = service;
        this.permisos = permisos;
    }

    @PostMapping
    public ResponseEntity<Void> registrar(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                          @Valid @RequestBody SolicitudHallazgo request) {
        String investigador = permisos.exigir(usuario, VerificadorPermisos.CREAR_HALLAZGO);
        Hallazgo guardado = service.registrar(request.conInvestigador(investigador));
        URI location = URI.create("/hallazgos/" + guardado.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public List<RespuestaHallazgo> listar(@RequestParam(required = false) String investigadorId) {
        List<RespuestaHallazgo> respuesta = new ArrayList<>();
        for (Hallazgo h : service.listar(investigadorId)) {
            respuesta.add(RespuestaHallazgo.desde(h));
        }
        return respuesta;
    }

    @GetMapping("/{id}")
    public RespuestaHallazgo obtener(@PathVariable UUID id) {
        return RespuestaHallazgo.desde(service.obtener(id));
    }

    @PatchMapping("/{id}")
    public RespuestaHallazgo modificar(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                       @PathVariable UUID id, @Valid @RequestBody SolicitudModificarHallazgo request) {
        String autor = permisos.exigir(usuario, VerificadorPermisos.EDITAR_HALLAZGO);
        return RespuestaHallazgo.desde(service.modificar(id, request.conAutor(autor)));
    }

    @PostMapping("/{id}/reclasificacion")
    public RespuestaHallazgo reclasificar(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                          @PathVariable UUID id, @Valid @RequestBody SolicitudReclasificarHallazgo request) {
        String autor = permisos.exigir(usuario, VerificadorPermisos.EDITAR_HALLAZGO);
        permisos.exigirAutorizador(request.autorizador());
        return RespuestaHallazgo.desde(service.reclasificar(id, request.conAutor(autor)));
    }
}
