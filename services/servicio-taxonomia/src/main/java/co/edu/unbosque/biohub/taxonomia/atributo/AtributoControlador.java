package co.edu.unbosque.biohub.taxonomia.atributo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.biohub.taxonomia.acceso.VerificadorPermisos;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/atributos")
public class AtributoControlador {

    private final AtributoServicio servicio;
    private final VerificadorPermisos permisos;

    public AtributoControlador(AtributoServicio servicio, VerificadorPermisos permisos) {
        this.servicio = servicio;
        this.permisos = permisos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtributoTaxonomico crear(
            @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuario,
            @Valid @RequestBody SolicitudAtributo request) {
        permisos.exigirEdicion(usuario);
        return servicio.crear(request);
    }

    @GetMapping
    public List<AtributoTaxonomico> listar() {
        return servicio.listar();
    }
}
