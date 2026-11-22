package co.edu.unbosque.biohub.taxonomia.atributo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/atributos")
public class AtributoControlador {

    private final AtributoServicio servicio;

    public AtributoControlador(AtributoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtributoTaxonomico crear(@Valid @RequestBody SolicitudAtributo request) {
        return servicio.crear(request);
    }

    @GetMapping
    public List<AtributoTaxonomico> listar() {
        return servicio.listar();
    }
}
