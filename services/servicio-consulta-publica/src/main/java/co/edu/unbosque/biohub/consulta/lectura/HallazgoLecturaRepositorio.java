package co.edu.unbosque.biohub.consulta.lectura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HallazgoLecturaRepositorio extends JpaRepository<HallazgoLectura, String>,
        JpaSpecificationExecutor<HallazgoLectura> {}
