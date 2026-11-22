package co.edu.unbosque.biohub.autorizacion.rol;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RolRepositorio extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
    List<Rol> findByNombreIn(Collection<String> nombres);
    boolean existsByNombre(String nombre);
}
