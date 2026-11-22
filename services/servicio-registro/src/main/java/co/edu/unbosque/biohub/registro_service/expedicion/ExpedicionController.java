package co.edu.unbosque.biohub.registro_service.expedicion;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/expediciones")
public class ExpedicionController {

    private final ExpedicionService servicio;

    public ExpedicionController(ExpedicionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<ExpedicionResponse> crear(@Valid @RequestBody ExpedicionRequest request) {
        Expedicion creada = servicio.crear(request);
        return ResponseEntity.created(URI.create("/expediciones/" + creada.getId()))
                .body(ExpedicionResponse.de(creada));
    }

    @GetMapping
public List<ExpedicionResponse> listar() {
    List<ExpedicionResponse> respuesta = new ArrayList<>();
    for (Expedicion e : servicio.listar()) {
        respuesta.add(ExpedicionResponse.de(e));
    }
    return respuesta;
}

    @GetMapping("/{id}")
    public ExpedicionResponse obtener(@PathVariable UUID id) {
        return ExpedicionResponse.de(servicio.obtener(id));
    }

    @PostMapping("/{id}/equipos")
    public ExpedicionResponse asignarEquipo(@PathVariable UUID id,
                                            @Valid @RequestBody AsignarEquipoRequest request) {
        return ExpedicionResponse.de(servicio.asignarEquipo(id, request.equipoId()));
    }
}