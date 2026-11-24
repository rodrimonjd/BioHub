package co.edu.unbosque.biohub.consulta.evento;

import co.edu.unbosque.biohub.consulta.evento.ProyectorHallazgos.Resultado;
import co.edu.unbosque.biohub.consulta.especie.CatalogoEspecies;
import co.edu.unbosque.biohub.consulta.especie.EspecieInfo;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLectura;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLecturaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProyectorHallazgosTest {

    private final Map<String, HallazgoLectura> tabla = new HashMap<>();
    private final CatalogoEspecies catalogo = mock(CatalogoEspecies.class);
    private ProyectorHallazgos proyector;

    @BeforeEach
    void preparar() {
        HallazgoLecturaRepositorio repositorio = mock(HallazgoLecturaRepositorio.class);
        when(repositorio.findById(any())).thenAnswer(i -> Optional.ofNullable(tabla.get(i.<String>getArgument(0))));
        when(repositorio.save(any())).thenAnswer(i -> {
            HallazgoLectura h = i.getArgument(0);
            tabla.put(h.getId(), h);
            return h;
        });
        proyector = new ProyectorHallazgos(repositorio, catalogo);
    }

    private static EventoEntrante registrado(String id, int version, Object fecha) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("especieId", "e1");
        d.put("especie", "Crocodylus intermedius");
        d.put("fecha", fecha);
        d.put("descripcion", "Adulto en la orilla");
        d.put("latitud", 4.5);
        d.put("longitud", -71.3);
        d.put("departamento", "Meta");
        d.put("region", null);
        d.put("version", version);
        return new EventoEntrante("ev-" + version, EventoEntrante.HALLAZGO_REGISTRADO, id, d);
    }

    private static EventoEntrante modificado(String id, String campo, String valor, int version) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("campo", campo);
        d.put("valorNuevo", valor);
        d.put("version", version);
        return new EventoEntrante("ev-m" + version, EventoEntrante.HALLAZGO_MODIFICADO, id, d);
    }

    @Test
    void registroCreaLaCopiaDeLectura() {
        proyector.aplicar(registrado("h1", 1, "2026-11-01T10:00:00Z"));

        HallazgoLectura h = tabla.get("h1");
        assertEquals("Crocodylus intermedius", h.getEspecie());
        assertEquals(Instant.parse("2026-11-01T10:00:00Z"), h.getFecha());
        assertEquals(4.5, h.getLatitud());
        assertEquals("Meta", h.getDepartamento());
        assertNull(h.getRegion());
        assertEquals(1, h.getVersion());
    }

    @Test
    void aceptaFechaComoSegundosDesde1970() {
        proyector.aplicar(registrado("h1", 1, 1761991200.5));
        assertEquals(Instant.ofEpochSecond(1761991200, 500_000_000), tabla.get("h1").getFecha());
    }

    @Test
    void modificacionCambiaElCampoYSubeLaVersion() {
        proyector.aplicar(registrado("h1", 1, "2026-11-01T10:00:00Z"));
        proyector.aplicar(modificado("h1", "region", "Sierra de la Macarena", 2));

        assertEquals("Sierra de la Macarena", tabla.get("h1").getRegion());
        assertEquals(2, tabla.get("h1").getVersion());
    }

    @Test
    void eventoRepetidoOAntiguoNoCambiaNada() {
        assertEquals(Resultado.APLICADO, proyector.aplicar(registrado("h1", 1, "2026-11-01T10:00:00Z")));
        assertEquals(Resultado.APLICADO, proyector.aplicar(modificado("h1", "descripcion", "Nueva", 2)));
        assertEquals(Resultado.IGNORADO, proyector.aplicar(modificado("h1", "descripcion", "Vieja repetida", 2)));
        assertEquals(Resultado.IGNORADO, proyector.aplicar(registrado("h1", 1, "2026-11-01T10:00:00Z")));

        assertEquals("Nueva", tabla.get("h1").getDescripcion());
        assertEquals(2, tabla.get("h1").getVersion());
    }

    @Test
    void reclasificacionBuscaElIdDeLaNuevaEspecie() {
        when(catalogo.porNombreCientifico("Panthera onca"))
                .thenReturn(Optional.of(new EspecieInfo("e2", "Panthera onca", "Jaguar", "NT")));
        proyector.aplicar(registrado("h1", 1, "2026-11-01T10:00:00Z"));
        proyector.aplicar(modificado("h1", "especie", "Panthera onca", 2));

        assertEquals("e2", tabla.get("h1").getEspecieId());
        assertEquals("Panthera onca", tabla.get("h1").getEspecie());
    }

    @Test
    void modificacionDeHallazgoDesconocidoSeIgnora() {
        assertEquals(Resultado.IGNORADO, proyector.aplicar(modificado("no-existe", "descripcion", "x", 2)));
        assertFalse(tabla.containsKey("no-existe"));
    }

    @Test
    void otrosTiposDeEventoSeIgnoran() {
        assertEquals(Resultado.IGNORADO,
                proyector.aplicar(new EventoEntrante("ev", "AVISTAMIENTO_REGISTRADO", "a1", Map.of())));
        assertFalse(tabla.containsKey("a1"));
    }

    @Test
    void registroSinFechaEsInvalido() {
        assertThrows(EventoInvalidoException.class, () -> proyector.aplicar(registrado("h1", 1, null)));
    }

    @Test
    void fechaMalEscritaEsInvalida() {
        assertThrows(EventoInvalidoException.class, () -> proyector.aplicar(registrado("h1", 1, "ayer")));
    }

    @Test
    void registroSinDepartamentoEsInvalido() {
        EventoEntrante evento = registrado("h1", 1, "2026-11-01T10:00:00Z");
        evento.datos().remove("departamento");
        assertThrows(EventoInvalidoException.class, () -> proyector.aplicar(evento));
        assertFalse(tabla.containsKey("h1"));
    }

    @Test
    void eventoSinIdAgregadoEsInvalido() {
        assertThrows(EventoInvalidoException.class, () -> proyector.aplicar(registrado(null, 1, "2026-11-01T10:00:00Z")));
    }
}
