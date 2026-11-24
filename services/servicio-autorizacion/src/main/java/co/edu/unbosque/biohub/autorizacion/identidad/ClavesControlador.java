package co.edu.unbosque.biohub.autorizacion.identidad;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/autorizacion")
public class ClavesControlador {

    private final ServicioTokens tokens;

    public ClavesControlador(ServicioTokens tokens) {
        this.tokens = tokens;
    }

    @GetMapping("/claves")
    public Map<String, Object> claves() {
        return tokens.clavesPublicas();
    }
}
