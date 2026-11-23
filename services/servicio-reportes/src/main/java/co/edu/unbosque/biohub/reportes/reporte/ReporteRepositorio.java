package co.edu.unbosque.biohub.reportes.reporte;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteRepositorio extends JpaRepository<Reporte, String> {

    List<Reporte> findByEstadoOrderByCreadoEnAsc(EstadoReporte estado);

    List<Reporte> findAllByOrderByCreadoEnDesc();

    List<Reporte> findBySolicitadoPorOrderByCreadoEnDesc(String solicitadoPor);
}
