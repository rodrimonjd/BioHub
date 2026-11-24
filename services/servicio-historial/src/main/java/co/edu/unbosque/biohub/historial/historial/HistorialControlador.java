package co.edu.unbosque.biohub.historial.historial;

import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaCambio;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaHistorial;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaIntegridad;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaVersion;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaVersiones;
import co.edu.unbosque.biohub.historial.permiso.VerificadorPermisos;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/historial")
public class HistorialControlador {

    private static final String CABECERA_USUARIO = "X-Usuario-Id";
    private static final String TIPO = "/{tipo:hallazgos|fichas}/{id}";

    private final ServicioConsultaHistorial servicio;
    private final VerificadorPermisos permisos;

    public HistorialControlador(ServicioConsultaHistorial servicio, VerificadorPermisos permisos) {
        this.servicio = servicio;
        this.permisos = permisos;
    }

    @GetMapping(TIPO)
    public VistaHistorial historial(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                    @PathVariable String tipo, @PathVariable String id) {
        permisos.exigir(usuario);
        return servicio.historial(tipoRegistro(tipo), id);
    }

    @GetMapping(TIPO + "/cambios/{version}")
    public VistaCambio cambio(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                              @PathVariable String tipo, @PathVariable String id, @PathVariable int version) {
        permisos.exigir(usuario);
        return servicio.cambio(tipoRegistro(tipo), id, version);
    }

    @GetMapping(TIPO + "/versiones")
    public VistaVersiones versiones(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                    @PathVariable String tipo, @PathVariable String id) {
        permisos.exigir(usuario);
        return servicio.versiones(tipoRegistro(tipo), id);
    }

    @GetMapping(TIPO + "/versiones/{version}")
    public VistaVersion version(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                @PathVariable String tipo, @PathVariable String id, @PathVariable int version) {
        permisos.exigir(usuario);
        return servicio.version(tipoRegistro(tipo), id, version);
    }

    @GetMapping(TIPO + "/integridad")
    public VistaIntegridad integridad(@RequestHeader(value = CABECERA_USUARIO, required = false) String usuario,
                                      @PathVariable String tipo, @PathVariable String id) {
        permisos.exigir(usuario);
        return servicio.integridad(tipoRegistro(tipo), id);
    }

    private static TipoRegistro tipoRegistro(String tipo) {
        return "hallazgos".equals(tipo) ? TipoRegistro.HALLAZGO : TipoRegistro.FICHA;
    }
}
