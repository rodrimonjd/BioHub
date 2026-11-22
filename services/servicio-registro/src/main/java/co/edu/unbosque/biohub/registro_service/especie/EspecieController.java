package co.edu.unbosque.biohub.registro_service.especie;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/especies")
public class EspecieController {

    private final EspecieRepository especies;

    public EspecieController(EspecieRepository especies) {
        this.especies = especies;
    }

    @GetMapping
    public List<EspecieResponse> listar(@RequestParam(defaultValue = "") String nombre) {
        return especies.buscar(nombre.trim()).stream()
                .map(EspecieResponse::desde)
                .toList();
    }
}