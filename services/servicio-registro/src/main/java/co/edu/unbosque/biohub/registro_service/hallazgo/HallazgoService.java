package co.edu.unbosque.biohub.registro_service.hallazgo;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.especie.EspecieRepository;
import co.edu.unbosque.biohub.registro_service.evento.EventoFactory;
import co.edu.unbosque.biohub.registro_service.evento.OutboxService;
import co.edu.unbosque.biohub.registro_service.evidencia.EvidenciaFactory;
import co.edu.unbosque.biohub.registro_service.evidencia.EvidenciaRequest;
import co.edu.unbosque.biohub.registro_service.expedicion.Expedicion;
import co.edu.unbosque.biohub.registro_service.expedicion.ExpedicionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class HallazgoService {

    private final HallazgoRepository hallazgos;
    private final EspecieRepository especies;
    private final ExpedicionRepository expediciones;
    private final OutboxService outbox;

    public HallazgoService(HallazgoRepository hallazgos, EspecieRepository especies,
                        ExpedicionRepository expediciones, OutboxService outbox) {
        this.hallazgos = hallazgos;
        this.especies = especies;
        this.expediciones = expediciones;
        this.outbox = outbox;
    }

    @Transactional
    public Hallazgo registrar(HallazgoRequest r) {
        Especie especie = especies.findById(r.especieId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especie no encontrada: " + r.especieId()));

        HallazgoBuilder builder = new HallazgoBuilder()
                .investigador(r.investigadorId())
                .especie(especie)
                .ficha(r.fichaId())
                .fecha(r.fecha())
                .descripcion(r.descripcion())
                .ubicacion(r.latitud(), r.longitud(), r.departamento(), r.region());

        if (r.expedicionId() != null) {
            Expedicion expedicion = expediciones.findById(r.expedicionId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Expedicion no encontrada"));
            builder.expedicion(expedicion);
        }

        if (r.evidencias() != null) {
            for (EvidenciaRequest e : r.evidencias()) {
                builder.agregarEvidencia(EvidenciaFactory.crear(
                        e.tipo(), e.valor(), e.unidad(), e.urlArchivo(), e.atributoTaxonomico()));
            }
        }

        Hallazgo guardado = hallazgos.save(builder.build());
        outbox.guardar(EventoFactory.hallazgoRegistrado(guardado));
        return guardado;
    }

    @Transactional(readOnly = true)
    public Hallazgo obtener(UUID id) {
        return hallazgos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Hallazgo no encontrado: " + id));
    }

    @Transactional
    public Hallazgo modificar(UUID id, ModificarHallazgoRequest r) {
        Hallazgo hallazgo = obtener(id);
        CambioHallazgo cambio = hallazgo.modificar(r.campo().trim(), r.valor().trim(), r.autor());
        outbox.guardar(EventoFactory.hallazgoModificado(hallazgo, cambio));
        return hallazgo;
    }

    @Transactional
    public Hallazgo reclasificar(UUID id, ReclasificarHallazgoRequest r) {
        Hallazgo hallazgo = obtener(id);
        Especie nueva = especies.findById(r.nuevaEspecieId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especie no encontrada: " + r.nuevaEspecieId()));
        CambioHallazgo cambio = hallazgo.reclasificar(nueva, r.motivo().trim(),
                r.autor(), r.autorizador());
        outbox.guardar(EventoFactory.hallazgoModificado(hallazgo, cambio));
        return hallazgo;
    }
}