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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/expediciones")
public class ExpedicionControlador {

    private final ExpedicionServicio servicio;
    private final VerificadorPermisos permisos;

    public ExpedicionControlador(ExpedicionServicio servicio, VerificadorPermisos permisos) {
        this.servicio = servicio;
        this.permisos = permisos;
    }

    @PostMapping
    public ResponseEntity<RespuestaExpedicion> crear(
            @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuario,
            @Valid @RequestBody SolicitudExpedicion request) {
        permisos.exigir(usuario, VerificadorPermisos.GESTIONAR_EQUIPOS);
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
    public RespuestaExpedicion asignarEquipo(
            @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuario,
            @PathVariable UUID id, @Valid @RequestBody SolicitudAsignarEquipo request) {
        permisos.exigir(usuario, VerificadorPermisos.GESTIONAR_EQUIPOS);
        return RespuestaExpedicion.de(servicio.asignarEquipo(id, request.equipoId()));
    }
}