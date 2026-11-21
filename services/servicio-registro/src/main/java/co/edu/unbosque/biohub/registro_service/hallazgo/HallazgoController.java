package co.edu.unbosque.biohub.registro_service.hallazgo;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/hallazgos")
public class HallazgoController {

    private final HallazgoService service;

    public HallazgoController(HallazgoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> registrar(@Valid @RequestBody HallazgoRequest request) {
        Hallazgo guardado = service.registrar(request);
        URI location = URI.create("/hallazgos/" + guardado.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public HallazgoResponse obtener(@PathVariable UUID id) {
        return HallazgoResponse.desde(service.obtener(id));
    }

    @PatchMapping("/{id}")
    public HallazgoResponse modificar(@PathVariable UUID id,
                                    @Valid @RequestBody ModificarHallazgoRequest request) {
        return HallazgoResponse.desde(service.modificar(id, request));
    }

    @PostMapping("/{id}/reclasificacion")
    public HallazgoResponse reclasificar(@PathVariable UUID id,
                                        @Valid @RequestBody ReclasificarHallazgoRequest request) {
        return HallazgoResponse.desde(service.reclasificar(id, request));
    }
}