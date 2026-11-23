package co.edu.unbosque.biohub.reportes.reporte;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.biohub.reportes.avistamiento.Avistamiento;
import co.edu.unbosque.biohub.reportes.avistamiento.AvistamientoRepositorio;

@Service
public class ServicioReportes {

    static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final ReporteRepositorio reportes;
    private final AvistamientoRepositorio avistamientos;

    public ServicioReportes(ReporteRepositorio reportes, AvistamientoRepositorio avistamientos) {
        this.reportes = reportes;
        this.avistamientos = avistamientos;
    }

    @Transactional
    public Reporte solicitar(SolicitudReporte s) {
        Reporte reporte = new Reporte(s.fechaInicio(), s.fechaFin(), s.departamento(), s.especieId(), s.solicitadoPor());
        return reportes.save(reporte);
    }

    @Transactional(readOnly = true)
    public Reporte obtener(String id) {
        return reportes.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Reporte no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Reporte> listar(String solicitadoPor) {
        if (solicitadoPor == null || solicitadoPor.isBlank()) {
            return reportes.findAllByOrderByCreadoEnDesc();
        }
        return reportes.findBySolicitadoPorOrderByCreadoEnDesc(solicitadoPor);
    }

    @Transactional(readOnly = true)
    public List<Reporte> enCola() {
        return reportes.findByEstadoOrderByCreadoEnAsc(EstadoReporte.EN_COLA);
    }

    @Transactional
    public void iniciarGeneracion(String id) {
        obtener(id).iniciarGeneracion();
    }

    @Transactional
    public void generar(String id) {
        Reporte reporte = obtener(id);
        Instant desde = reporte.getFechaInicio().atStartOfDay(ZONA).toInstant();
        Instant hasta = reporte.getFechaFin().plusDays(1).atStartOfDay(ZONA).toInstant();
        List<Avistamiento> delPeriodo = avistamientos.findByFechaHoraGreaterThanEqualAndFechaHoraLessThan(desde, hasta);
        reporte.generar(delPeriodo);
    }

    @Transactional
    public void marcarError(String id, String motivo) {
        obtener(id).marcarError(motivo);
    }

    @Transactional(readOnly = true)
    public Reporte paraExportar(String id) {
        Reporte reporte = obtener(id);
        if (reporte.getEstado() != EstadoReporte.LISTO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El reporte no esta listo para exportar (estado " + reporte.getEstado() + ")");
        }
        return reporte;
    }
}
