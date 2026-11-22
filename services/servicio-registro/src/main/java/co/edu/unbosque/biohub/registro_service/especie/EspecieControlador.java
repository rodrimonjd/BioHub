package co.edu.unbosque.biohub.registro_service.especie;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/especies")
public class EspecieControlador {

    private final EspecieRepositorio especies;

    public EspecieControlador(EspecieRepositorio especies) {
        this.especies = especies;
    }

    @GetMapping
    public List<RespuestaEspecie> listar(@RequestParam(defaultValue = "") String nombre) {
        return especies.buscar(nombre.trim()).stream().map(RespuestaEspecie::desde).toList();
    }
}