package co.edu.unbosque.biohub.registro_service.especie;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EspecieRepository extends JpaRepository<Especie, UUID> { 
    @Query("select e from Especie e " +
       "where lower(e.nombreCientifico) like lower(concat('%', :texto, '%')) " +
       "   or lower(e.nombreComun) like lower(concat('%', :texto, '%')) " +
       "order by e.nombreCientifico")
    List<Especie> buscar(@Param("texto") String texto);
}