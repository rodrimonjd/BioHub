package co.edu.unbosque.biohub.reportes.reporte;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.biohub.reportes.acceso.VerificadorPermisos;
import co.edu.unbosque.biohub.reportes.compartido.DatosInvalidosException;
import jakarta.validation.Valid;

/** Consultar y exportar reportes exige reporte:ver; pedir uno nuevo, reporte:generar. */
@RestController
@RequestMapping("/reportes")
public class ReporteControlador {

    private final ServicioReportes servicio;
    private final VerificadorPermisos permisos;

    public ReporteControlador(ServicioReportes servicio, VerificadorPermisos permisos) {
        this.servicio = servicio;
        this.permisos = permisos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Reporte solicitar(@Valid @RequestBody SolicitudReporte solicitud,
                             @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuarioId) {
        String usuario = permisos.exigir(usuarioId, VerificadorPermisos.GENERAR_REPORTES);
        return servicio.solicitar(solicitud.conSolicitante(usuario));
    }

    @GetMapping
    public List<Reporte> listar(@RequestParam(required = false) String solicitadoPor,
                                @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuarioId) {
        permisos.exigir(usuarioId, VerificadorPermisos.VER_REPORTES);
        return servicio.listar(solicitadoPor);
    }

    @GetMapping("/{id}")
    public Reporte obtener(@PathVariable String id,
                           @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuarioId) {
        permisos.exigir(usuarioId, VerificadorPermisos.VER_REPORTES);
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/exportar")
    public ResponseEntity<?> exportar(@PathVariable String id, @RequestParam(defaultValue = "pdf") String formato,
                                      @RequestHeader(value = VerificadorPermisos.CABECERA_USUARIO, required = false) String usuarioId) {
        permisos.exigir(usuarioId, VerificadorPermisos.VER_REPORTES);
        Reporte reporte = servicio.paraExportar(id);
        if (formato.equalsIgnoreCase("pdf")) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(DocumentoPdfReporte.generar(reporte));
        }
        if (formato.equalsIgnoreCase("json")) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-" + id + ".json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(reporte);
        }
        throw new DatosInvalidosException("Formato no soportado: " + formato + " (use pdf o json)");
    }
}
