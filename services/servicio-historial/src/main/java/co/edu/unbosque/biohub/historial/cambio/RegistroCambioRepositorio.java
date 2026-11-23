package co.edu.unbosque.biohub.historial.cambio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistroCambioRepositorio extends JpaRepository<RegistroCambio, Long> {

    boolean existsByIdEvento(String idEvento);

    /** Todos los cambios del registro en orden cronológico (por versión). */
    List<RegistroCambio> findByTipoRegistroAndIdRegistroOrderByVersionAsc(TipoRegistro tipo, String idRegistro);

    Optional<RegistroCambio> findTopByTipoRegistroAndIdRegistroOrderByVersionDesc(TipoRegistro tipo, String idRegistro);

    Optional<RegistroCambio> findByTipoRegistroAndIdRegistroAndVersion(TipoRegistro tipo, String idRegistro, int version);
}
