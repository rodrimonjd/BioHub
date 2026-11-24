package co.edu.unbosque.biohub.taxonomia.ficha;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.biohub.taxonomia.acceso.VerificadorPermisos;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/fichas")
public class FichaControlador {

    private final FichaServicio servicio;
    private final VerificadorPermisos permisos;

    public FichaControlador(FichaServicio servicio, VerificadorPermisos permisos) {
        this.servicio = servicio;
        this.permisos = permisos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FichaTaxonomica crear(
            @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuario,
            @Valid @RequestBody SolicitudesFicha.CrearFicha request) {
        return servicio.crear(request.conAutor(permisos.exigirEdicion(usuario)));
    }

    @GetMapping
    public List<FichaTaxonomica> listar(@RequestParam(required = false) UUID especieId, @RequestParam(defaultValue = "false") boolean incluirInactivas) {
        return servicio.listar(especieId, incluirInactivas);
    }

    @GetMapping("/{id}")
    public FichaTaxonomica obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @PutMapping("/{id}/atributos/{nombre}")
    public FichaTaxonomica modificarValor(
            @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuario,
            @PathVariable String id, @PathVariable String nombre,
            @Valid @RequestBody SolicitudesFicha.ModificarValor request) {
        return servicio.modificarValor(id, nombre, request.conAutor(permisos.exigirEdicion(usuario)));
    }

    @PostMapping("/{id}/atributos")
    public FichaTaxonomica incorporarAtributo(
            @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuario,
            @PathVariable String id, @Valid @RequestBody SolicitudesFicha.IncorporarAtributo request) {
        return servicio.incorporarAtributo(id, request.conAutor(permisos.exigirEdicion(usuario)));
    }

    @PostMapping("/{id}/desactivacion")
    public FichaTaxonomica desactivar(
            @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuario,
            @PathVariable String id, @Valid @RequestBody SolicitudesFicha.DesactivarFicha request) {
        return servicio.desactivar(id, request.conAutor(permisos.exigirDesactivacion(usuario)));
    }
}
