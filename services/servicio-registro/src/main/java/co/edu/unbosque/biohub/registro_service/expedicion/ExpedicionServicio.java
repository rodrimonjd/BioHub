package co.edu.unbosque.biohub.registro_service.expedicion;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.biohub.registro_service.equipo.EquipoCampo;
import co.edu.unbosque.biohub.registro_service.equipo.EquipoServicio;
import co.edu.unbosque.biohub.registro_service.evento.FabricaEventos;
import co.edu.unbosque.biohub.registro_service.evento.OutboxServicio;

@Service
public class ExpedicionServicio {

    private final ExpedicionRepositorio expediciones;
    private final EquipoServicio equipoService;
    private final OutboxServicio outbox;

    public ExpedicionServicio(ExpedicionRepositorio expediciones, EquipoServicio equipoService, OutboxServicio outbox) {
        this.expediciones = expediciones;
        this.equipoService = equipoService;
        this.outbox = outbox;
    }

    @Transactional
    public Expedicion crear(SolicitudExpedicion r) {
        try {
            return expediciones.save( new Expedicion(r.nombre(), r.zona(), r.fechaInicio(), r.fechaFin(), r.liderId()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Expedicion obtener(UUID id) {
        return expediciones.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expedicion no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Expedicion> listar(String liderId) {
        if (liderId == null || liderId.isBlank()) {
            return expediciones.findAll();
        }
        return expediciones.findByLiderId(liderId);
    }

    @Transactional
    public Expedicion asignarEquipo(UUID expedicionId, UUID equipoId) {
        Expedicion expedicion = obtener(expedicionId);
        EquipoCampo equipo = equipoService.obtener(equipoId);
        try {
            expedicion.asignarEquipo(equipo);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        outbox.guardar(FabricaEventos.equipoAsignado(expedicion, equipo));
        return expedicion;
    }
}