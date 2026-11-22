package co.edu.unbosque.biohub.registro_service.hallazgo;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/hallazgos")
public class HallazgoControlador {

    private final HallazgoServicio service;

    public HallazgoControlador(HallazgoServicio service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> registrar(@Valid @RequestBody SolicitudHallazgo request) {
        Hallazgo guardado = service.registrar(request);
        URI location = URI.create("/hallazgos/" + guardado.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public RespuestaHallazgo obtener(@PathVariable UUID id) {
        return RespuestaHallazgo.desde(service.obtener(id));
    }

    @PatchMapping("/{id}")
    public RespuestaHallazgo modificar(@PathVariable UUID id, @Valid @RequestBody SolicitudModificarHallazgo request) {
        return RespuestaHallazgo.desde(service.modificar(id, request));
    }

    @PostMapping("/{id}/reclasificacion")
    public RespuestaHallazgo reclasificar(@PathVariable UUID id,@Valid @RequestBody SolicitudReclasificarHallazgo request) {
        return RespuestaHallazgo.desde(service.reclasificar(id, request));
    }
}