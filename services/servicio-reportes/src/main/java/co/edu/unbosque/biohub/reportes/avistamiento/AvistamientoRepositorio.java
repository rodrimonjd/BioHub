package co.edu.unbosque.biohub.reportes.avistamiento;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AvistamientoRepositorio extends JpaRepository<Avistamiento, String> {

    List<Avistamiento> findByFechaHoraGreaterThanEqualAndFechaHoraLessThan(Instant desde, Instant hasta);
}
