package co.edu.unbosque.biohub.autorizacion.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRolRepositorio extends JpaRepository<UsuarioRol, Long> {
    List<UsuarioRol> findByUsuarioId(String usuarioId);
    Optional<UsuarioRol> findByUsuarioIdAndRolNombre(String usuarioId, String nombreRol);
}
