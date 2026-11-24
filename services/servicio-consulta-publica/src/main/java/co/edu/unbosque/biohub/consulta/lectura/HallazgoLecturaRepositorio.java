package co.edu.unbosque.biohub.consulta.lectura;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface HallazgoLecturaRepositorio extends JpaRepository<HallazgoLectura, String> {
    @Query("""
            select h from HallazgoLectura h
            where (:especieId is null or h.especieId = :especieId)
              and (:departamento is null or lower(h.departamento) = lower(:departamento))
              and (:especie is null or lower(h.especie) like lower(concat('%', :especie, '%')))
              and (:desde is null or h.fecha >= :desde)
              and (:hasta is null or h.fecha < :hasta)
            order by h.fecha desc, h.id
            """)
    Page<HallazgoLectura> buscar(@Param("especieId") String especieId,
                                 @Param("departamento") String departamento,
                                 @Param("especie") String especie,
                                 @Param("desde") Instant desde,
                                 @Param("hasta") Instant hasta,
                                 Pageable pagina);
}
