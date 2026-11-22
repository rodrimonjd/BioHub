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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/expediciones")
public class ExpedicionControlador {

    private final ExpedicionServicio servicio;

    public ExpedicionControlador(ExpedicionServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<RespuestaExpedicion> crear(@Valid @RequestBody SolicitudExpedicion request) {
        Expedicion creada = servicio.crear(request);
        return ResponseEntity.created(URI.create("/expediciones/" + creada.getId())).body(RespuestaExpedicion.de(creada));
    }

    @GetMapping
public List<RespuestaExpedicion> listar(@RequestParam(required = false) String liderId) {
    List<RespuestaExpedicion> respuesta = new ArrayList<>();
    for (Expedicion e : servicio.listar(liderId)) {
        respuesta.add(RespuestaExpedicion.de(e));
    }
    return respuesta;
}

    @GetMapping("/{id}")
    public RespuestaExpedicion obtener(@PathVariable UUID id) {
        return RespuestaExpedicion.de(servicio.obtener(id));
    }

    @PostMapping("/{id}/equipos")
    public RespuestaExpedicion asignarEquipo(@PathVariable UUID id, @Valid @RequestBody SolicitudAsignarEquipo request) {
        return RespuestaExpedicion.de(servicio.asignarEquipo(id, request.equipoId()));
    }
}