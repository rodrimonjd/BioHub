package co.edu.unbosque.biohub.registro_service.hallazgo;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.especie.EspecieRepository;
import co.edu.unbosque.biohub.registro_service.evidencia.EvidenciaFactory;
import co.edu.unbosque.biohub.registro_service.evidencia.EvidenciaRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class HallazgoService {

    private final HallazgoRepository hallazgos;
    private final EspecieRepository especies;

    public HallazgoService(HallazgoRepository hallazgos, EspecieRepository especies) {
        this.hallazgos = hallazgos;
        this.especies = especies;
    }

    @Transactional
    public Hallazgo registrar(HallazgoRequest r) {
        Especie especie = especies.findById(r.especieId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especie no encontrada: " + r.especieId()));

        HallazgoBuilder builder = new HallazgoBuilder()
                .investigador(r.investigadorId())   // TODO: tomarlo del token
                .especie(especie)
                .ficha(r.fichaId())
                .fecha(r.fecha())
                .descripcion(r.descripcion())
                .ubicacion(r.latitud(), r.longitud(), r.departamento(), r.region());

        if (r.evidencias() != null) {
            for (EvidenciaRequest e : r.evidencias()) {
                builder.agregarEvidencia(EvidenciaFactory.crear(
                        e.tipo(), e.valor(), e.unidad(), e.urlArchivo(), e.atributoTaxonomico()));
            }
        }

        return hallazgos.save(builder.build());
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
        hallazgo.modificar(r.campo().trim(), r.valor().trim(), r.autor());
        return hallazgo;
    }

    @Transactional
    public Hallazgo reclasificar(UUID id, ReclasificarHallazgoRequest r) {
        Hallazgo hallazgo = obtener(id);
        Especie nueva = especies.findById(r.nuevaEspecieId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especie no encontrada: " + r.nuevaEspecieId()));
        hallazgo.reclasificar(nueva, r.motivo().trim(), r.autor(), r.autorizador()); 
        return hallazgo;
    }
}