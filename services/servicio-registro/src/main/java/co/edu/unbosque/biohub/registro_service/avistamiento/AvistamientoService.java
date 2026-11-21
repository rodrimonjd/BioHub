package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.especie.EspecieRepository;
import co.edu.unbosque.biohub.registro_service.evento.EventoFactory;
import co.edu.unbosque.biohub.registro_service.evento.OutboxService;

@Service
public class AvistamientoService {

    private final AvistamientoRepository avistamientos;
    private final EspecieRepository especies;
    private final OutboxService outbox;

    public AvistamientoService(AvistamientoRepository avistamientos, EspecieRepository especies, OutboxService outbox) {
        this.avistamientos = avistamientos;
        this.especies = especies;
        this.outbox = outbox;
    }

    @Transactional
    public Avistamiento registrar(AvistamientoRequest r) {
        Especie especie = especies.findById(r.especieId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especie no encontrada: " + r.especieId()));

        Avistamiento a = new Avistamiento(r.investigadorId(), especie, r.fechaHora(),
                r.cantidad(), r.latitud(), r.longitud(), r.departamento(), r.region());
        
        Avistamiento guardado = avistamientos.save(a);
        outbox.guardar(EventoFactory.avistamientoRegistrado(guardado));
        return guardado;
    }

    public Avistamiento obtener(UUID id) {
    return avistamientos.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Avistamiento no encontrado: " + id));
}

    public List<Avistamiento> listar() {
        return avistamientos.findAllByOrderByFechaHoraDesc();
    }
}