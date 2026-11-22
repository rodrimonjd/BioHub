package co.edu.unbosque.biohub.autorizacion.permiso;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PermisoRepositorio extends JpaRepository<Permiso, Long> {
    List<Permiso> findByCodigoIn(Collection<String> codigos);
}
