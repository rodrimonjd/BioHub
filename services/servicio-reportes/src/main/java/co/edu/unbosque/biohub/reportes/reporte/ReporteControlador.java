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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.biohub.reportes.compartido.DatosInvalidosException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/reportes")
public class ReporteControlador {

    private final ServicioReportes servicio;

    public ReporteControlador(ServicioReportes servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Reporte solicitar(@Valid @RequestBody SolicitudReporte solicitud) {
        return servicio.solicitar(solicitud);
    }

    @GetMapping
    public List<Reporte> listar(@RequestParam(required = false) String solicitadoPor) {
        return servicio.listar(solicitadoPor);
    }

    @GetMapping("/{id}")
    public Reporte obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @GetMapping("/{id}/exportar")
    public ResponseEntity<?> exportar(@PathVariable String id, @RequestParam(defaultValue = "pdf") String formato) {
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
