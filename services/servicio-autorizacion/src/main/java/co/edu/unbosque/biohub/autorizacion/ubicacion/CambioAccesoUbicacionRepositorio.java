package co.edu.unbosque.biohub.autorizacion.ubicacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CambioAccesoUbicacionRepositorio extends JpaRepository<CambioAccesoUbicacion, Long> {
    List<CambioAccesoUbicacion> findAllByOrderByIdDesc();
}
