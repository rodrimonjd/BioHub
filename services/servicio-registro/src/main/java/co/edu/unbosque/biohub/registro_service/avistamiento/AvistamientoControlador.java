package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/avistamientos")
public class AvistamientoControlador {

    private final AvistamientoServicio service;
    private final VerificadorPermisos permisos;

    public AvistamientoControlador(AvistamientoServicio service, VerificadorPermisos permisos) {
        this.service = service;
        this.permisos = permisos;
    }

    @PostMapping
    public ResponseEntity<RespuestaAvistamiento> registrar(
            @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuario,
            @Valid @RequestBody SolicitudAvistamiento request) {
        String investigador = permisos.exigir(usuario, VerificadorPermisos.CREAR_HALLAZGO);
        Avistamiento guardado = service.registrar(request.conInvestigador(investigador));
        return ResponseEntity.status(HttpStatus.CREATED).body(RespuestaAvistamiento.desde(guardado));
    }

    @GetMapping("/{id}")
    public RespuestaAvistamiento obtener(@PathVariable UUID id) {
        Avistamiento a = service.obtener(id);
        return RespuestaAvistamiento.desde(a);
    }

    @GetMapping
    public List<RespuestaAvistamiento> listar() {
        List<RespuestaAvistamiento> respuesta = new ArrayList<>();
        for (Avistamiento a : service.listar()) {
            respuesta.add(RespuestaAvistamiento.desde(a));
        }
        return respuesta;
    }
}
