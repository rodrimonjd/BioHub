package co.edu.unbosque.biohub.historial.historial;

import co.edu.unbosque.biohub.historial.cambio.ConstructorRegistroCambio;
import co.edu.unbosque.biohub.historial.cambio.RegistroCambio;
import co.edu.unbosque.biohub.historial.cambio.RegistroCambioRepositorio;
import co.edu.unbosque.biohub.historial.cambio.TipoCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;
import co.edu.unbosque.biohub.historial.compartido.NoEncontradoException;
import co.edu.unbosque.biohub.historial.historial.Vistas.ResumenVersion;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaCambio;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaHistorial;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaIntegridad;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaVersion;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaVersiones;
import co.edu.unbosque.biohub.historial.version.VersionRegistro;
import co.edu.unbosque.biohub.historial.version.VersionRegistroRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Consultas del historial: cambios, versiones y verificación de integridad. Solo lectura. */
@Service
@Transactional(readOnly = true)
public class ServicioConsultaHistorial {

    private static final TypeReference<Map<String, Object>> TIPO_MAPA = new TypeReference<>() {};

    private final RegistroCambioRepositorio cambios;
    private final VersionRegistroRepositorio versiones;
    private final JsonMapper json;

    public ServicioConsultaHistorial(RegistroCambioRepositorio cambios, VersionRegistroRepositorio versiones,
                                     JsonMapper json) {
        this.cambios = cambios;
        this.versiones = versiones;
        this.json = json;
    }

    public VistaHistorial historial(TipoRegistro tipo, String id) {
        List<RegistroCambio> lista = cambiosDe(tipo, id);
        int versionActual = lista.getLast().getVersion();
        int modificaciones = (int) lista.stream().filter(c -> c.getTipoCambio() != TipoCambio.CREACION).count();
        List<Integer> faltantes = versionesFaltantes(lista);
        String mensaje = modificaciones == 0
                ? "El registro no ha sido modificado: no existen cambios registrados después de su creación"
                : modificaciones + " modificación(es) registrada(s) en orden cronológico";
        if (!faltantes.isEmpty()) {
            mensaje += ". Atención: faltan las versiones " + faltantes + " en el historial";
        }
        return new VistaHistorial(tipo, id, versionActual, modificaciones, faltantes.isEmpty(), faltantes, mensaje,
                lista.stream().map(c -> VistaCambio.desde(c)).toList());
    }

    public VistaCambio cambio(TipoRegistro tipo, String id, int version) {
        return cambios.findByTipoRegistroAndIdRegistroAndVersion(tipo, id, version)
                .map(c -> VistaCambio.desde(c))
                .orElseThrow(() -> new NoEncontradoException(
                        "No hay un cambio con la versión " + version + " para " + nombre(tipo, id)));
    }

    public VistaVersiones versiones(TipoRegistro tipo, String id) {
        List<RegistroCambio> lista = cambiosDe(tipo, id);
        int versionActual = lista.getLast().getVersion();
        String mensaje = lista.size() == 1
                ? "Solo existe la versión actual: el registro no tiene versiones anteriores"
                : lista.size() + " versiones conservadas, ordenadas cronológicamente";
        List<ResumenVersion> resumen = lista.stream()
                .map(c -> new ResumenVersion(c.getVersion(), c.getOcurrioEn(), c.getAutor(), c.getTipoCambio(),
                        c.describir()))
                .toList();
        return new VistaVersiones(tipo, id, versionActual, mensaje, resumen);
    }

    public VistaVersion version(TipoRegistro tipo, String id, int numero) {
        VersionRegistro v = versiones.findByTipoRegistroAndIdRegistroAndVersion(tipo, id, numero)
                .orElseThrow(() -> new NoEncontradoException(
                        "No existe la versión " + numero + " de " + nombre(tipo, id)));
        int actual = versiones.findTopByTipoRegistroAndIdRegistroOrderByVersionDesc(tipo, id)
                .map(x -> x.getVersion()).orElse(numero);
        String mensaje = v.getEstado() == null
                ? "No se conoce el estado completo de esta versión porque falta una versión anterior en el historial"
                : (numero == actual ? "Versión actual" : "Versión histórica (la actual es la " + actual + ")");
        return new VistaVersion(tipo, id, numero, numero == actual, v.getCreadaEn(), v.getAutor(),
                leer(v.getEstado()), mensaje);
    }

    /**
     * Recalcula la cadena de hashes. Detecta cambios alterados (el hash no coincide), cambios borrados
     * (la cadena se rompe) y versiones faltantes.
     */
    public VistaIntegridad integridad(TipoRegistro tipo, String id) {
        List<RegistroCambio> lista = cambiosDe(tipo, id);
        Map<Integer, String> estados = new HashMap<>();
        for (VersionRegistro v : versiones.findByTipoRegistroAndIdRegistroOrderByVersionAsc(tipo, id)) {
            estados.put(v.getVersion(), v.getEstado());
        }
        List<String> problemas = new ArrayList<>();
        String hashEsperadoAnterior = null;
        for (RegistroCambio c : lista) {
            if (!Objects.equals(c.getHashAnterior(), hashEsperadoAnterior)) {
                problemas.add("Versión " + c.getVersion() + ": la cadena está rota (falta o se alteró un cambio anterior)");
            }
            if (!estados.containsKey(c.getVersion())) {
                problemas.add("Versión " + c.getVersion() + ": falta el estado guardado de esta versión");
            }
            String recalculado = ConstructorRegistroCambio.calcularHash(c, estados.get(c.getVersion()));
            if (!recalculado.equals(c.getHash())) {
                problemas.add("Versión " + c.getVersion() + ": los datos guardados no coinciden con su hash (fueron alterados)");
            }
            hashEsperadoAnterior = c.getHash();
        }
        versionesFaltantes(lista).forEach(v -> problemas.add("Versión " + v + ": no está en el historial"));
        return new VistaIntegridad(tipo, id, problemas.isEmpty(), lista.size(), problemas);
    }

    // ---- auxiliares ----

    private List<RegistroCambio> cambiosDe(TipoRegistro tipo, String id) {
        List<RegistroCambio> lista = cambios.findByTipoRegistroAndIdRegistroOrderByVersionAsc(tipo, id);
        if (lista.isEmpty()) {
            throw new NoEncontradoException("No hay historial registrado para " + nombre(tipo, id));
        }
        return lista;
    }

    private static List<Integer> versionesFaltantes(List<RegistroCambio> lista) {
        Set<Integer> presentes = lista.stream().map(c -> c.getVersion()).collect(Collectors.toSet());
        int maxima = lista.getLast().getVersion();
        List<Integer> faltantes = new ArrayList<>();
        for (int v = 1; v <= maxima; v++) {
            if (!presentes.contains(v)) faltantes.add(v);
        }
        return faltantes;
    }

    private Map<String, Object> leer(String estadoJson) {
        if (estadoJson == null) return null;
        try {
            return json.readValue(estadoJson, TIPO_MAPA);
        } catch (JacksonException e) {
            throw new IllegalStateException("Estado guardado ilegible", e);
        }
    }

    private static String nombre(TipoRegistro tipo, String id) {
        return (tipo == TipoRegistro.HALLAZGO ? "el hallazgo " : "la ficha ") + id;
    }
}
