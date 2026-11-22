package co.edu.unbosque.biohub.consulta.lectura;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HallazgoLecturaRepositorio extends JpaRepository<HallazgoLectura, String> {

    /** Filtros opcionales: si un parámetro es null no se aplica. */
    @Query("""
            select h from HallazgoLectura h
            where (:especieId is null or h.especieId = :especieId)
              and (:departamento is null or lower(h.departamento) = lower(:departamento))
              and (:especie is null or lower(h.especie) like lower(concat('%', :especie, '%')))
            order by h.fecha desc, h.id
            """)
    Page<HallazgoLectura> buscar(@Param("especieId") String especieId,
                                 @Param("departamento") String departamento,
                                 @Param("especie") String especie,
                                 Pageable pagina);
}
