package co.edu.unbosque.biohub.registro_service.hallazgo;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.especie.EspecieRepositorio;
import co.edu.unbosque.biohub.registro_service.evento.FabricaEventos;
import co.edu.unbosque.biohub.registro_service.evento.OutboxServicio;
import co.edu.unbosque.biohub.registro_service.evidencia.Evidencia;
import co.edu.unbosque.biohub.registro_service.evidencia.FabricaEvidencia;
import co.edu.unbosque.biohub.registro_service.evidencia.SolicitudEvidencia;
import co.edu.unbosque.biohub.registro_service.expedicion.Expedicion;
import co.edu.unbosque.biohub.registro_service.expedicion.ExpedicionRepositorio;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class HallazgoServicio {

    private final HallazgoRepositorio hallazgos;
    private final EspecieRepositorio especies;
    private final ExpedicionRepositorio expediciones;
    private final OutboxServicio outbox;

    public HallazgoServicio(HallazgoRepositorio hallazgos, EspecieRepositorio especies, ExpedicionRepositorio expediciones, OutboxServicio outbox) {
        this.hallazgos = hallazgos;
        this.especies = especies;
        this.expediciones = expediciones;
        this.outbox = outbox;
    }

    @Transactional
    public Hallazgo registrar(SolicitudHallazgo r) {
        Especie especie = especies.findById(r.especieId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Especie no encontrada: " + r.especieId()));

        ConstructorHallazgo builder = new ConstructorHallazgo()
                .investigador(r.investigadorId())
                .especie(especie)
                .ficha(r.fichaId())
                .fecha(r.fecha())
                .descripcion(r.descripcion())
                .ubicacion(r.latitud(), r.longitud(), r.departamento(), r.region());

        if (r.expedicionId() != null) {
            Expedicion expedicion = expediciones.findById(r.expedicionId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expedicion no encontrada"));
            builder.expedicion(expedicion);
        }

        if (r.evidencias() != null) {
            for (SolicitudEvidencia e : r.evidencias()) {
                builder.agregarEvidencia(FabricaEvidencia.crear(e.tipo(), e.valor(), e.unidad(), e.urlArchivo(), e.atributoTaxonomico()));
            }
        }

        Hallazgo guardado = hallazgos.save(builder.build());
        outbox.guardar(FabricaEventos.hallazgoRegistrado(guardado));
        return guardado;
    }

    @Transactional(readOnly = true)
    public List<Hallazgo> listar(String investigadorId) {
        if (investigadorId == null || investigadorId.isBlank()) {
            return hallazgos.findAllByOrderByCreadoEnDesc();
        }
        return hallazgos.findByInvestigadorIdOrderByCreadoEnDesc(investigadorId.trim());
    }

    @Transactional(readOnly = true)
    public Hallazgo obtener(UUID id) {
        return hallazgos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hallazgo no encontrado: " + id));
    }

    @Transactional
    public Hallazgo modificar(UUID id, SolicitudModificarHallazgo r) {
        Hallazgo hallazgo = obtener(id);
        CambioHallazgo cambio = hallazgo.modificar(r.campo().trim(), r.valor().trim(), r.autor());
        outbox.guardar(FabricaEventos.hallazgoModificado(hallazgo, cambio));
        return hallazgo;
    }

    @Transactional
    public Hallazgo agregarEvidencia(UUID id, SolicitudEvidencia e, String autor) {
        Hallazgo hallazgo = obtener(id);
        Evidencia evidencia = FabricaEvidencia.crear(e.tipo(), e.valor(), e.unidad(), e.urlArchivo(), e.atributoTaxonomico());
        CambioHallazgo cambio = hallazgo.registrarEvidencia(evidencia, autor);
        hallazgos.saveAndFlush(hallazgo);
        outbox.guardar(FabricaEventos.hallazgoModificado(hallazgo, cambio));
        return hallazgo;
    }

    @Transactional
    public Hallazgo reclasificar(UUID id, SolicitudReclasificarHallazgo r) {
        Hallazgo hallazgo = obtener(id);
        Especie nueva = especies.findById(r.nuevaEspecieId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Especie no encontrada: " + r.nuevaEspecieId()));
        CambioHallazgo cambio = hallazgo.reclasificar(nueva, r.motivo().trim(), r.autor(), r.autorizador());
        outbox.guardar(FabricaEventos.hallazgoModificado(hallazgo, cambio));
        return hallazgo;
    }
}