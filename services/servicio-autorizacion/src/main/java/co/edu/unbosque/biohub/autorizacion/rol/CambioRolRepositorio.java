package co.edu.unbosque.biohub.autorizacion.rol;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CambioRolRepositorio extends JpaRepository<CambioRol, Long> {
    List<CambioRol> findAllByOrderByIdDesc();
}
