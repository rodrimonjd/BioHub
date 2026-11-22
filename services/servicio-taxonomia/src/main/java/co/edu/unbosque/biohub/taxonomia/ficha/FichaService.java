package co.edu.unbosque.biohub.taxonomia.ficha;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.biohub.taxonomia.atributo.AtributoService;
import co.edu.unbosque.biohub.taxonomia.evento.EventoDominio;
import co.edu.unbosque.biohub.taxonomia.evento.EventoFactory;
import co.edu.unbosque.biohub.taxonomia.evento.EventoPendiente;
import tools.jackson.databind.json.JsonMapper;

@Service
public class FichaService {

    private final FichaRepository fichas;
    private final AtributoService atributos;
    private final JsonMapper json;

    public FichaService(FichaRepository fichas, AtributoService atributos, JsonMapper json) {
        this.fichas = fichas;
        this.atributos = atributos;
        this.json = json;
    }

    public FichaTaxonomica crear(FichaRequests.CrearFicha r) {
        if (fichas.existsByEspecieIdAndActivaTrue(r.especieId().toString())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La especie ya tiene una ficha activa; desactivela antes de crear otra");
        }
        FichaBuilder builder = new FichaBuilder()
                .especie(r.especieId())
                .autor(r.autor());
        for (FichaRequests.ValorRequest v : r.atributos()) {
            builder.atributo(atributos.buscarPorNombre(v.nombre()), v.valor());
        }
        FichaTaxonomica ficha = builder.build();
        return guardar(ficha, EventoFactory.fichaCreada(ficha));
    }

    public FichaTaxonomica obtener(String id) {
        return fichas.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ficha no encontrada: " + id));
    }

    /** Por defecto solo las activas; con incluirInactivas tambien las desactivadas. */
    public List<FichaTaxonomica> listar(UUID especieId, boolean incluirInactivas) {
        List<FichaTaxonomica> todas = especieId == null
                ? fichas.findAll()
                : fichas.findByEspecieId(especieId.toString());
        List<FichaTaxonomica> resultado = new ArrayList<>();
        for (FichaTaxonomica ficha : todas) {
            if (incluirInactivas || ficha.isActiva()) {
                resultado.add(ficha);
            }
        }
        return resultado;
    }

    public FichaTaxonomica modificarValor(String id, String nombre, FichaRequests.ModificarValor r) {
        FichaTaxonomica ficha = obtener(id);
        String anterior = ficha.modificarValor(nombre, r.valor());
        return guardar(ficha, EventoFactory.fichaModificada(ficha, nombre.trim(), anterior,
                ficha.valorDe(nombre), r.autor()));
    }

    public FichaTaxonomica incorporarAtributo(String id, FichaRequests.IncorporarAtributo r) {
        FichaTaxonomica ficha = obtener(id);
        ficha.incorporarAtributo(atributos.buscarPorNombre(r.nombre()), r.valor());
        return guardar(ficha, EventoFactory.fichaModificada(ficha, r.nombre().trim(), null,
                ficha.valorDe(r.nombre()), r.autor()));
    }

    public FichaTaxonomica desactivar(String id, FichaRequests.DesactivarFicha r) {
        FichaTaxonomica ficha = obtener(id);
        ficha.desactivar();
        return guardar(ficha, EventoFactory.fichaDesactivada(ficha, r.autor(), r.motivo()));
    }

    /** Guarda la ficha junto con su evento pendiente, en una sola escritura (Outbox). */
    private FichaTaxonomica guardar(FichaTaxonomica ficha, EventoDominio evento) {
        ficha.agregarEvento(new EventoPendiente(evento.tipo(), json.writeValueAsString(evento)));
        try {
            return fichas.save(ficha);
        } catch (OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La ficha fue modificada al mismo tiempo por otra operacion; intente de nuevo");
        }
    }
}
