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
public class AvistamientoController {

    private final AvistamientoService service;

    public AvistamientoController(AvistamientoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AvistamientoResponse> registrar(@Valid @RequestBody AvistamientoRequest request) {
        Avistamiento guardado = service.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AvistamientoResponse.desde(guardado));
    }

    @GetMapping("/{id}")
    public AvistamientoResponse obtener(@PathVariable UUID id) {
        Avistamiento a = service.obtener(id);
        return AvistamientoResponse.desde(a);
    }

    @GetMapping
    public List<AvistamientoResponse> listar() {
        List<AvistamientoResponse> respuesta = new ArrayList<>();
        for (Avistamiento a : service.listar()) {
            respuesta.add(AvistamientoResponse.desde(a));
        }
        return respuesta;
    }
}
