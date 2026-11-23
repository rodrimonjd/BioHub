package co.edu.unbosque.biohub.reportes.reporte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GeneradorReportes {

    private static final Logger log = LoggerFactory.getLogger(GeneradorReportes.class);

    private final ServicioReportes servicio;

    public GeneradorReportes(ServicioReportes servicio) {
        this.servicio = servicio;
    }

    @Scheduled(fixedDelayString = "${reportes.intervalo-generacion-ms}")
    public void generarPendientes() {
        for (Reporte reporte : servicio.enCola()) {
            String id = reporte.getId();
            try {
                servicio.iniciarGeneracion(id);
                servicio.generar(id);
                log.info("Reporte {} generado", id);
            } catch (RuntimeException e) {
                log.warn("No se pudo generar el reporte {}: {}", id, e.getMessage());
                servicio.marcarError(id, "Error al generar el reporte: " + e.getMessage());
            }
        }
    }
}
