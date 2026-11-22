package co.edu.unbosque.biohub.consulta.consulta;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consulta")
public class ConsultaControlador {

    private static final String CABECERA_USUARIO = "X-Usuario-Id";
    private final ServicioConsulta servicio;

    public ConsultaControlador(ServicioConsulta servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/hallazgos")
    public PaginaHallazgos listar(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuarioId,
                                  @RequestParam(required = false) String especieId,
                                  @RequestParam(required = false) String departamento,
                                  @RequestParam(required = false) String especie,
                                  @RequestParam(defaultValue = "0") int pagina,
                                  @RequestParam(defaultValue = "20") int tamano) {
        return servicio.listar(usuarioId, especieId, departamento, especie, pagina, tamano);
    }

    @GetMapping("/hallazgos/{id}")
    public VistaHallazgoPublico obtener(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuarioId,
                                        @PathVariable String id) {
        return servicio.obtener(usuarioId, id);
    }
}
