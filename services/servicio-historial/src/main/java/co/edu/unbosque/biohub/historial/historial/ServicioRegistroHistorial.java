package co.edu.unbosque.biohub.historial.historial;

import co.edu.unbosque.biohub.historial.cambio.ConstructorRegistroCambio;
import co.edu.unbosque.biohub.historial.cambio.RegistroCambio;
import co.edu.unbosque.biohub.historial.cambio.RegistroCambioIncompletoException;
import co.edu.unbosque.biohub.historial.cambio.RegistroCambioRepositorio;
import co.edu.unbosque.biohub.historial.cambio.TipoCambio;
import co.edu.unbosque.biohub.historial.evento.CambioPropuesto;
import co.edu.unbosque.biohub.historial.evento.EventoEntrante;
import co.edu.unbosque.biohub.historial.evento.EventoInvalidoException;
import co.edu.unbosque.biohub.historial.evento.TraductorEventos;
import co.edu.unbosque.biohub.historial.version.VersionRegistro;
import co.edu.unbosque.biohub.historial.version.VersionRegistroRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Optional;

@Service
public class ServicioRegistroHistorial {

    private static final Logger log = LoggerFactory.getLogger(ServicioRegistroHistorial.class);
    private static final TypeReference<Map<String, Object>> TIPO_MAPA = new TypeReference<>() {};

    public enum Resultado { GUARDADO, IGNORADO }

    private final RegistroCambioRepositorio cambios;
    private final VersionRegistroRepositorio versiones;
    private final TraductorEventos traductor;
    private final JsonMapper json;

    public ServicioRegistroHistorial(RegistroCambioRepositorio cambios, VersionRegistroRepositorio versiones,
                                     TraductorEventos traductor, JsonMapper json) {
        this.cambios = cambios;
        this.versiones = versiones;
        this.traductor = traductor;
        this.json = json;
    }

    @Transactional
    public Resultado registrar(EventoEntrante evento, String cargaUtil) {
        if (evento.idEvento() != null && cambios.existsByIdEvento(evento.idEvento())) {
            return Resultado.IGNORADO;
        }
        CambioPropuesto propuesto = traductor.traducir(evento);

        Optional<RegistroCambio> ultimo = cambios.findTopByTipoRegistroAndIdRegistroOrderByVersionDesc(
                propuesto.tipoRegistro(), propuesto.idRegistro());
        if (ultimo.isPresent() && ultimo.get().getVersion() >= propuesto.version()) {
            log.info("Versión {} de {} {} ya registrada; se ignora", propuesto.version(),
                    propuesto.tipoRegistro(), propuesto.idRegistro());
            return Resultado.IGNORADO;
        }
        if (ultimo.isPresent() && propuesto.version() != ultimo.get().getVersion() + 1) {
            log.warn("Hueco en el historial de {} {}: se esperaba la versión {} y llegó la {}",
                    propuesto.tipoRegistro(), propuesto.idRegistro(), ultimo.get().getVersion() + 1, propuesto.version());
        }

        Map<String, Object> estadoAnterior = propuesto.tipoCambio() == TipoCambio.CREACION ? null
                : versiones.findByTipoRegistroAndIdRegistroAndVersion(propuesto.tipoRegistro(),
                        propuesto.idRegistro(), propuesto.version() - 1)
                .map(v -> leer(v.getEstado()))
                .orElse(null);
        Map<String, Object> estadoNuevo = propuesto.estadoResultante(estadoAnterior);
        String estadoJson = estadoNuevo == null ? null : escribir(estadoNuevo);

        RegistroCambio cambio;
        try {
            cambio = new ConstructorRegistroCambio()
                    .evento(evento.idEvento(), cargaUtil)
                    .registro(propuesto.tipoRegistro(), propuesto.idRegistro(), propuesto.version())
                    .tipo(propuesto.tipoCambio())
                    .valores(propuesto.campo(), propuesto.valorAnterior(), propuesto.valorNuevo())
                    .motivo(propuesto.motivo())
                    .responsables(propuesto.autor(), propuesto.autorizador())
                    .ocurrioEn(propuesto.ocurrioEn())
                    .encadenarCon(ultimo.map(c -> c.getHash()).orElse(null))
                    .estadoResultante(estadoJson)
                    .construir();
        } catch (RegistroCambioIncompletoException e) {
            throw new EventoInvalidoException("Cambio incompleto: " + e.getMessage(), e);
        }
        cambios.save(cambio);
        versiones.save(new VersionRegistro(evento.idEvento(), propuesto.tipoRegistro(), propuesto.idRegistro(),
                propuesto.version(), estadoJson, propuesto.autor(), cambio.getOcurrioEn()));
        return Resultado.GUARDADO;
    }

    private Map<String, Object> leer(String estadoJson) {
        if (estadoJson == null) return null;
        try {
            return json.readValue(estadoJson, TIPO_MAPA);
        } catch (JacksonException e) {
            throw new IllegalStateException("Estado guardado ilegible", e);
        }
    }

    private String escribir(Map<String, Object> estado) {
        try {
            return json.writeValueAsString(estado);
        } catch (JacksonException e) {
            throw new EventoInvalidoException("No se pudo serializar el estado del registro", e);
        }
    }
}
