package co.edu.unbosque.biohub.registro_service.especie;

import co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/especies")
public class EspecieControlador {

    private final EspecieRepositorio especies;
    private final EspecieServicio servicio;
    private final VerificadorPermisos permisos;

    public EspecieControlador(EspecieRepositorio especies, EspecieServicio servicio, VerificadorPermisos permisos) {
        this.especies = especies;
        this.servicio = servicio;
        this.permisos = permisos;
    }

    @GetMapping
    public List<RespuestaEspecie> listar(@RequestParam(defaultValue = "") String nombre) {
        return especies.buscar(nombre.trim()).stream().map(RespuestaEspecie::desde).toList();
    }

    @PostMapping
    public ResponseEntity<RespuestaEspecie> crear(
            @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuario,
            @Valid @RequestBody SolicitudEspecie solicitud) {
        permisos.exigir(usuario, VerificadorPermisos.CREAR_HALLAZGO);
        Especie creada = servicio.crear(solicitud);
        return ResponseEntity.created(URI.create("/especies/" + creada.getId()))
                .body(RespuestaEspecie.desde(creada));
    }
}
