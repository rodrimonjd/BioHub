package co.edu.unbosque.biohub.registro_service.equipo;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/equipos")
public class EquipoController {

    private final EquipoService servicio;

    public EquipoController(EquipoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<EquipoResponse> crear(@Valid @RequestBody EquipoRequest request) {
        EquipoCampo creado = servicio.crear(request);
        return ResponseEntity.created(URI.create("/equipos/" + creado.getId()))
                .body(EquipoResponse.de(creado));
    }

    @GetMapping
    public List<EquipoResponse> listar() {
        return servicio.listar().stream().map(EquipoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public EquipoResponse obtener(@PathVariable UUID id) {
        return EquipoResponse.de(servicio.obtener(id));
    }

    @PutMapping("/{id}/responsable")
    public EquipoResponse asignarResponsable(@PathVariable UUID id, @Valid @RequestBody ResponsableRequest request) {
        return EquipoResponse.de(servicio.asignarResponsable(id, request.investigadorId()));
    }
}