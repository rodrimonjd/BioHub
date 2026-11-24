package co.edu.unbosque.biohub.historial.evento;

import co.edu.unbosque.biohub.historial.cambio.TipoCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TraductorEventosTest {

    private static final Instant FECHA = Instant.parse("2026-11-10T08:00:00Z");
    private final TraductorEventos traductor = new TraductorEventos();

    private static EventoEntrante evento(String tipo, String autorizador, Map<String, Object> datos) {
        return new EventoEntrante("ev-1", tipo, "id-1", FECHA, "inv-1", autorizador, datos);
    }

    private static Map<String, Object> datos(Object... claveValor) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < claveValor.length; i += 2) m.put((String) claveValor[i], claveValor[i + 1]);
        return m;
    }

    @Test
    void hallazgoRegistradoEsUnaCreacionConTodoSuEstado() {
        CambioPropuesto c = traductor.traducir(evento(EventoEntrante.HALLAZGO_REGISTRADO, null,
                datos("especie", "Ara macao", "departamento", "Meta", "version", 1)));
        assertEquals(TipoRegistro.HALLAZGO, c.tipoRegistro());
        assertEquals(TipoCambio.CREACION, c.tipoCambio());
        assertEquals(1, c.version());
        Map<String, Object> estado = c.estadoResultante(null);
        assertEquals("Meta", estado.get("departamento"));
        assertFalse(estado.containsKey("version"));
    }

    @Test
    void hallazgoModificadoCambiaSoloEseCampoSobreUnaCopia() {
        CambioPropuesto c = traductor.traducir(evento(EventoEntrante.HALLAZGO_MODIFICADO, null,
                datos("campo", "region", "valorAnterior", null, "valorNuevo", "Llanos", "version", 2)));
        assertEquals(TipoCambio.MODIFICACION, c.tipoCambio());
        Map<String, Object> anterior = Map.of("region", "Macarena", "departamento", "Meta");
        Map<String, Object> nuevo = c.estadoResultante(anterior);
        assertEquals("Llanos", nuevo.get("region"));
        assertEquals("Meta", nuevo.get("departamento"));
        assertEquals("Macarena", anterior.get("region"));
    }

    @Test
    void cambioDeEspecieEsUnaReclasificacionConAutorizador() {
        CambioPropuesto c = traductor.traducir(evento(EventoEntrante.HALLAZGO_MODIFICADO, "admin",
                datos("campo", "especie", "valorAnterior", "Ara macao", "valorNuevo", "Panthera onca",
                        "motivo", "Revisión", "version", 3)));
        assertEquals(TipoCambio.RECLASIFICACION, c.tipoCambio());
        assertEquals("admin", c.autorizador());
        Map<String, Object> nuevo = c.estadoResultante(Map.of("especie", "Ara macao", "especieId", "e1"));
        assertEquals("Panthera onca", nuevo.get("especie"));
        assertFalse(nuevo.containsKey("especieId"));
    }

    @Test
    void sinEstadoAnteriorNoSeInventaElEstado() {
        CambioPropuesto c = traductor.traducir(evento(EventoEntrante.HALLAZGO_MODIFICADO, null,
                datos("campo", "region", "valorNuevo", "Llanos", "version", 5)));
        assertNull(c.estadoResultante(null));
    }

    @Test
    void fichaCreadaModificadaYDesactivada() {
        CambioPropuesto creada = traductor.traducir(evento(EventoEntrante.FICHA_CREADA, null,
                datos("especieId", "e1", "atributos", Map.of("peso", "90"), "version", 1)));
        Map<String, Object> v1 = creada.estadoResultante(null);
        assertEquals(TipoRegistro.FICHA, creada.tipoRegistro());
        assertEquals(true, v1.get("activa"));

        CambioPropuesto modificada = traductor.traducir(evento(EventoEntrante.FICHA_MODIFICADA, null,
                datos("campo", "peso", "valorAnterior", "90", "valorNuevo", "95", "version", 2)));
        Map<String, Object> v2 = modificada.estadoResultante(v1);
        assertEquals("95", ((Map<?, ?>) v2.get("atributos")).get("peso"));
        assertEquals("90", ((Map<?, ?>) v1.get("atributos")).get("peso"));

        CambioPropuesto desactivada = traductor.traducir(evento(EventoEntrante.FICHA_DESACTIVADA, null,
                datos("motivo", "Duplicada", "version", 3)));
        assertEquals(TipoCambio.DESACTIVACION, desactivada.tipoCambio());
        Map<String, Object> v3 = desactivada.estadoResultante(v2);
        assertEquals(false, v3.get("activa"));
        assertEquals("Duplicada", v3.get("motivoDesactivacion"));
    }

    @Test
    void eventosIncompletosSonInvalidos() {
        assertThrows(EventoInvalidoException.class, () -> traductor.traducir(
                evento(EventoEntrante.HALLAZGO_REGISTRADO, null, datos("especie", "X"))));
        assertThrows(EventoInvalidoException.class, () -> traductor.traducir(
                new EventoEntrante("ev", EventoEntrante.HALLAZGO_REGISTRADO, "h1", FECHA, null, null,
                        datos("version", 1))));
        assertThrows(EventoInvalidoException.class, () -> traductor.traducir(
                evento(EventoEntrante.HALLAZGO_MODIFICADO, null, datos("valorNuevo", "x", "version", 2))));
        assertThrows(EventoInvalidoException.class, () -> traductor.traducir(
                evento("AVISTAMIENTO_REGISTRADO", null, datos("version", 1))));
    }
}
