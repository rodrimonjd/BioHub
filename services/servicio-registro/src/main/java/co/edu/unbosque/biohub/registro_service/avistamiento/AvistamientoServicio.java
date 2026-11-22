package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.especie.EspecieRepositorio;
import co.edu.unbosque.biohub.registro_service.evento.FabricaEventos;
import co.edu.unbosque.biohub.registro_service.evento.OutboxServicio;
import co.edu.unbosque.biohub.registro_service.evidencia.DatosInvalidosException;

@Service
public class AvistamientoServicio {

    private final AvistamientoRepositorio avistamientos;
    private final EspecieRepositorio especies;
    private final OutboxServicio outbox;

    public AvistamientoServicio(AvistamientoRepositorio avistamientos, EspecieRepositorio especies, OutboxServicio outbox) {
        this.avistamientos = avistamientos;
        this.especies = especies;
        this.outbox = outbox;
    }

    @Transactional
    public Avistamiento registrar(SolicitudAvistamiento r) {
        Especie especie = especies.findById(r.especieId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especie no encontrada: " + r.especieId()));

        Avistamiento a = new Avistamiento(r.investigadorId(), especie, r.fechaHora(),
                r.cantidad(), r.latitud(), r.longitud(), r.departamento(), r.region());
        if (!a.validarDatos()) {
            throw new DatosInvalidosException(
                    "Datos del avistamiento invalidos: revise la fecha, la cantidad y que latitud y longitud vengan juntas");
        }

        Avistamiento guardado = avistamientos.save(a);
        outbox.guardar(FabricaEventos.avistamientoRegistrado(guardado));
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