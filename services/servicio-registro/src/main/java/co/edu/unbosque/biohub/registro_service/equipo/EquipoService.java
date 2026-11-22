package co.edu.unbosque.biohub.registro_service.equipo;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EquipoService {

    private final EquipoCampoRepository equipos;

    public EquipoService(EquipoCampoRepository equipos) {
        this.equipos = equipos;
    }

    @Transactional
    public EquipoCampo crear(EquipoRequest r) {
        if (equipos.existsByNumeroSerie(r.numeroSerie())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un equipo con ese numero de serie");
        }
        try {
            return equipos.save(new EquipoCampo(r.numeroSerie(), r.tipo(), r.modelo()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public EquipoCampo obtener(UUID id) {
        return equipos.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<EquipoCampo> listar() {
        return equipos.findAll();
    }

    @Transactional
    public EquipoCampo asignarResponsable(UUID id, String investigadorId) {
        EquipoCampo equipo = obtener(id);
        try {
            equipo.asignarResponsable(investigadorId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return equipo;   // JPA guarda el cambio al cerrar la transaccion
    }
}