package co.edu.unbosque.biohub.historial.version;

import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VersionRegistroRepositorio extends JpaRepository<VersionRegistro, Long> {

    List<VersionRegistro> findByTipoRegistroAndIdRegistroOrderByVersionAsc(TipoRegistro tipo, String idRegistro);

    Optional<VersionRegistro> findByTipoRegistroAndIdRegistroAndVersion(TipoRegistro tipo, String idRegistro, int version);

    Optional<VersionRegistro> findTopByTipoRegistroAndIdRegistroOrderByVersionDesc(TipoRegistro tipo, String idRegistro);
}
