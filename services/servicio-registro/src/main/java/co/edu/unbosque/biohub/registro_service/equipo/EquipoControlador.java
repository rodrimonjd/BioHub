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
public class EquipoControlador {

    private final EquipoServicio servicio;

    public EquipoControlador(EquipoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<RespuestaEquipo> crear(@Valid @RequestBody SolicitudEquipo request) {
        EquipoCampo creado = servicio.crear(request);
        return ResponseEntity.created(URI.create("/equipos/" + creado.getId()))
                .body(RespuestaEquipo.de(creado));
    }

    @GetMapping
    public List<RespuestaEquipo> listar() {
        return servicio.listar().stream().map(RespuestaEquipo::de).toList();
    }

    @GetMapping("/{id}")
    public RespuestaEquipo obtener(@PathVariable UUID id) {
        return RespuestaEquipo.de(servicio.obtener(id));
    }

    @PutMapping("/{id}/responsable")
    public RespuestaEquipo asignarResponsable(@PathVariable UUID id, @Valid @RequestBody SolicitudResponsable request) {
        return RespuestaEquipo.de(servicio.asignarResponsable(id, request.investigadorId()));
    }
}