package co.edu.unbosque.biohub.registro_service.expedicion;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.biohub.registro_service.equipo.EquipoCampo;
import co.edu.unbosque.biohub.registro_service.equipo.EquipoService;

@Service
public class ExpedicionService {

    private final ExpedicionRepository expediciones;
    private final EquipoService equipoService;

    public ExpedicionService(ExpedicionRepository expediciones, EquipoService equipoService) {
        this.expediciones = expediciones;
        this.equipoService = equipoService;
    }

    @Transactional
    public Expedicion crear(ExpedicionRequest r) {
        try {
            return expediciones.save(
                    new Expedicion(r.nombre(), r.zona(), r.fechaInicio(), r.fechaFin()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Expedicion obtener(UUID id) {
        return expediciones.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expedicion no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Expedicion> listar() {
        return expediciones.findAll();
    }

    @Transactional
    public Expedicion asignarEquipo(UUID expedicionId, UUID equipoId) {
        Expedicion expedicion = obtener(expedicionId);
        EquipoCampo equipo = equipoService.obtener(equipoId);
        try {
            expedicion.asignarEquipo(equipo);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return expedicion;
    }
}