package co.edu.unbosque.biohub.taxonomia.ficha;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.biohub.taxonomia.atributo.AtributoServicio;
import co.edu.unbosque.biohub.taxonomia.evento.EventoDominio;
import co.edu.unbosque.biohub.taxonomia.evento.FabricaEventos;
import co.edu.unbosque.biohub.taxonomia.evento.EventoPendiente;
import tools.jackson.databind.json.JsonMapper;

@Service
public class FichaServicio {

    private final FichaRepositorio fichas;
    private final AtributoServicio atributos;
    private final JsonMapper json;

    public FichaServicio(FichaRepositorio fichas, AtributoServicio atributos, JsonMapper json) {
        this.fichas = fichas;
        this.atributos = atributos;
        this.json = json;
    }

    public FichaTaxonomica crear(SolicitudesFicha.CrearFicha r) {
        if (fichas.existsByEspecieIdAndActivaTrue(r.especieId().toString())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La especie ya tiene una ficha activa; desactivela antes de crear otra");
        }
        ConstructorFicha builder = new ConstructorFicha().especie(r.especieId()).autor(r.autor());
        for (SolicitudesFicha.SolicitudValor v : r.atributos()) {
            builder.atributo(atributos.buscarPorNombre(v.nombre()), v.valor());
        }
        FichaTaxonomica ficha = builder.build();
        return guardar(ficha, FabricaEventos.fichaCreada(ficha));
    }

    public FichaTaxonomica obtener(String id) {
        return fichas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ficha no encontrada: " + id));
    }

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

    public FichaTaxonomica modificarValor(String id, String nombre, SolicitudesFicha.ModificarValor r) {
        FichaTaxonomica ficha = obtener(id);
        String anterior = ficha.modificarValor(nombre, r.valor());
        return guardar(ficha, FabricaEventos.fichaModificada(ficha, nombre.trim(), anterior, ficha.valorDe(nombre), r.autor()));
    }

    public FichaTaxonomica incorporarAtributo(String id, SolicitudesFicha.IncorporarAtributo r) {
        FichaTaxonomica ficha = obtener(id);
        ficha.incorporarAtributo(atributos.buscarPorNombre(r.nombre()), r.valor());
        return guardar(ficha, FabricaEventos.fichaModificada(ficha, r.nombre().trim(), null, ficha.valorDe(r.nombre()), r.autor()));
    }

    public FichaTaxonomica desactivar(String id, SolicitudesFicha.DesactivarFicha r) {
        FichaTaxonomica ficha = obtener(id);
        ficha.desactivar();
        return guardar(ficha, FabricaEventos.fichaDesactivada(ficha, r.autor(), r.motivo()));
    }

    private FichaTaxonomica guardar(FichaTaxonomica ficha, EventoDominio evento) {
        ficha.agregarEvento(new EventoPendiente(evento.tipo(), json.writeValueAsString(evento)));
        try {
            return fichas.save(ficha);
        } catch (OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La ficha fue modificada al mismo tiempo por otra operacion; intente de nuevo");
        }
    }
}
