package co.edu.unbosque.biohub.registro_service.hallazgo;

import co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos;
import co.edu.unbosque.biohub.registro_service.evidencia.SolicitudEvidencia;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

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
    public List<RespuestaHallazgo> listar(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                          @RequestParam(required = false) String investigadorId) {
        Predicate<UUID> verExacta = permisos.ubicacionExacta(usuario);
        List<RespuestaHallazgo> respuesta = new ArrayList<>();
        for (Hallazgo h : service.listar(investigadorId)) {
            RespuestaHallazgo r = RespuestaHallazgo.desde(h);
            respuesta.add(verExacta.test(r.especieId()) ? r : r.sinCoordenadas());
        }
        return respuesta;
    }

    @GetMapping("/{id}")
    public RespuestaHallazgo obtener(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                     @PathVariable UUID id) {
        return vista(service.obtener(id), usuario);
    }

    @PatchMapping("/{id}")
    public RespuestaHallazgo modificar(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                       @PathVariable UUID id, @Valid @RequestBody SolicitudModificarHallazgo request) {
        String autor = permisos.exigir(usuario, VerificadorPermisos.EDITAR_HALLAZGO);
        return vista(service.modificar(id, request.conAutor(autor)), autor);
    }

    @PostMapping("/{id}/evidencias")
    public RespuestaHallazgo agregarEvidencia(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                              @PathVariable UUID id, @Valid @RequestBody SolicitudEvidencia request) {
        String autor = permisos.exigir(usuario, VerificadorPermisos.EDITAR_HALLAZGO);
        return vista(service.agregarEvidencia(id, request, autor), autor);
    }

    @PostMapping("/{id}/reclasificacion")
    public RespuestaHallazgo reclasificar(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                          @PathVariable UUID id, @Valid @RequestBody SolicitudReclasificarHallazgo request) {
        String autor = permisos.exigir(usuario, VerificadorPermisos.EDITAR_HALLAZGO);
        permisos.exigirAutorizador(request.autorizador());
        return vista(service.reclasificar(id, request.conAutor(autor)), autor);
    }

    private RespuestaHallazgo vista(Hallazgo h, String usuario) {
        RespuestaHallazgo r = RespuestaHallazgo.desde(h);
        return permisos.ubicacionExacta(usuario).test(r.especieId()) ? r : r.sinCoordenadas();
    }
}
