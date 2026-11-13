package co.edu.unbosque.biohub.registro_service.avistamiento;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
