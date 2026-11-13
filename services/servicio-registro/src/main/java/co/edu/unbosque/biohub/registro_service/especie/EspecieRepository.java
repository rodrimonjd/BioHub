package co.edu.unbosque.biohub.registro_service.especie;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecieRepository extends JpaRepository<Especie, UUID> { }