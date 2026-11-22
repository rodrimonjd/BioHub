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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/avistamientos")
public class AvistamientoControlador {

    private final AvistamientoServicio service;

    public AvistamientoControlador(AvistamientoServicio service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RespuestaAvistamiento> registrar(@Valid @RequestBody SolicitudAvistamiento request) {
        Avistamiento guardado = service.registrar(request);
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
