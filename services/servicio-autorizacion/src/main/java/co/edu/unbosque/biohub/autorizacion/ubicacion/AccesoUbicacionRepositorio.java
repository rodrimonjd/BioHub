package co.edu.unbosque.biohub.autorizacion.ubicacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccesoUbicacionRepositorio extends JpaRepository<AccesoUbicacion, Long> {
    List<AccesoUbicacion> findByUsuarioId(String usuarioId);
    Optional<AccesoUbicacion> findByUsuarioIdAndEspecieId(String usuarioId, String especieId);
    List<AccesoUbicacion> findAllByOrderByUsuarioIdAscEspecieIdAsc();
}
