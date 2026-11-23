package co.edu.unbosque.biohub.historial.historial;

import co.edu.unbosque.biohub.historial.cambio.TipoCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;
import co.edu.unbosque.biohub.historial.compartido.NoEncontradoException;
import co.edu.unbosque.biohub.historial.evento.EventoEntrante;
import co.edu.unbosque.biohub.historial.evento.EventoInvalidoException;
import co.edu.unbosque.biohub.historial.evento.TraductorEventos;
import co.edu.unbosque.biohub.historial.historial.ServicioRegistroHistorial.Resultado;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaHistorial;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaIntegridad;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaVersion;
import co.edu.unbosque.biohub.historial.version.VersionRegistro;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guardado y consulta del historial juntos, sobre el almacén en memoria. */
class ServicioHistorialTest {

    private final AlmacenEnMemoria almacen = new AlmacenEnMemoria();
    private final JsonMapper json = JsonMapper.builder().build();
    private final ServicioRegistroHistorial registro =
            new ServicioRegistroHistorial(almacen.repoCambios, almacen.repoVersiones, new TraductorEventos(), json);
    private final ServicioConsultaHistorial consulta =
            new ServicioConsultaHistorial(almacen.repoCambios, almacen.repoVersiones, json);

    private static final String H = "h1";

    private static Map<String, Object> datos(Object... claveValor) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < claveValor.length; i += 2) m.put((String) claveValor[i], claveValor[i + 1]);
        return m;
    }

    private Resultado registrado(String idEvento) {
        return registro.registrar(new EventoEntrante(idEvento, EventoEntrante.HALLAZGO_REGISTRADO, H,
                Instant.parse("2026-11-01T10:00:00Z"), "inv-1", null,
                datos("especie", "Ara macao", "descripcion", "Pareja", "departamento", "Meta", "region", "Llanos",
                        "version", 1)), "{}");
    }

    private Resultado modificado(String idEvento, String campo, String anterior, String nuevo, int version,
                                 String autorizador, String motivo) {
        return registro.registrar(new EventoEntrante(idEvento, EventoEntrante.HALLAZGO_MODIFICADO, H,
                Instant.parse("2026-11-0" + Math.min(version + 1, 9) + "T10:00:00Z"), "inv-2", autorizador,
                datos("campo", campo, "valorAnterior", anterior, "valorNuevo", nuevo, "motivo", motivo,
                        "version", version)), "{}");
    }

    @Test
    void hallazgoSinModificacionesLoIndica() {
        registrado("e1");
        VistaHistorial h = consulta.historial(TipoRegistro.HALLAZGO, H);
        assertEquals(0, h.totalModificaciones());
        assertTrue(h.mensaje().startsWith("El registro no ha sido modificado"));
        assertEquals(1, h.cambios().size());
        assertTrue(consulta.versiones(TipoRegistro.HALLAZGO, H).mensaje().startsWith("Solo existe la versión actual"));
    }

    @Test
    void historialCronologicoConResponsablesYValores() {
        registrado("e1");
        modificado("e2", "region", "Llanos", "Altillanura", 2, null, null);
        modificado("e3", "especie", "Ara macao", "Panthera onca", 3, "admin", "Error de identificación");

        VistaHistorial h = consulta.historial(TipoRegistro.HALLAZGO, H);
        assertEquals(3, h.versionActual());
        assertEquals(2, h.totalModificaciones());
        assertTrue(h.completo());
        assertEquals(List.of(1, 2, 3), h.cambios().stream().map(c -> c.version()).toList());
        var reclasificacion = h.cambios().get(2);
        assertEquals(TipoCambio.RECLASIFICACION, reclasificacion.tipoCambio());
        assertEquals("inv-2", reclasificacion.autor());
        assertEquals("admin", reclasificacion.autorizador());
        assertEquals("Ara macao", reclasificacion.valorAnterior());

        var detalle = consulta.cambio(TipoRegistro.HALLAZGO, H, 2);
        assertEquals("region", detalle.campo());
        assertEquals("Llanos", detalle.valorAnterior());
        assertEquals("Altillanura", detalle.valorNuevo());
    }

    @Test
    void cadaVersionConservaSuEstadoSinSobrescribirLaAnterior() {
        registrado("e1");
        modificado("e2", "region", "Llanos", "Altillanura", 2, null, null);

        VistaVersion v1 = consulta.version(TipoRegistro.HALLAZGO, H, 1);
        VistaVersion v2 = consulta.version(TipoRegistro.HALLAZGO, H, 2);
        assertEquals("Llanos", v1.estado().get("region"));
        assertFalse(v1.esActual());
        assertEquals("Altillanura", v2.estado().get("region"));
        assertTrue(v2.esActual());
        assertEquals("Pareja", v2.estado().get("descripcion"));
    }

    @Test
    void eventoRepetidoOVersionYaConocidaSeIgnora() {
        assertEquals(Resultado.GUARDADO, registrado("e1"));
        assertEquals(Resultado.IGNORADO, registrado("e1"));      // mismo evento
        assertEquals(Resultado.IGNORADO, registrado("e1-bis"));  // otro evento con la misma versión
        assertEquals(1, almacen.cambios.size());
    }

    @Test
    void huecoDeVersionesSeGuardaYSeAvisa() {
        registrado("e1");
        modificado("e4", "region", "X", "Y", 4, null, null); // faltan la 2 y la 3

        VistaHistorial h = consulta.historial(TipoRegistro.HALLAZGO, H);
        assertFalse(h.completo());
        assertEquals(List.of(2, 3), h.versionesFaltantes());
        VistaVersion v4 = consulta.version(TipoRegistro.HALLAZGO, H, 4);
        assertNull(v4.estado()); // no se inventa un estado sin conocer la versión 3
        assertFalse(consulta.integridad(TipoRegistro.HALLAZGO, H).integro());
    }

    @Test
    void reclasificacionSinAutorizadorEsUnEventoInvalido() {
        registrado("e1");
        assertThrows(EventoInvalidoException.class,
                () -> modificado("e2", "especie", "Ara macao", "Panthera onca", 2, null, "Revisión"));
        assertEquals(1, almacen.cambios.size());
    }

    @Test
    void integridadCorrectaYDeteccionDeAlteraciones() {
        registrado("e1");
        modificado("e2", "region", "Llanos", "Altillanura", 2, null, null);
        VistaIntegridad ok = consulta.integridad(TipoRegistro.HALLAZGO, H);
        assertTrue(ok.integro());
        assertEquals(2, ok.cambiosVerificados());

        // alguien cambia a mano el estado guardado de la versión 2
        VersionRegistro original = almacen.versiones.get(1);
        almacen.versiones.set(1, new VersionRegistro(original.getIdEvento(), original.getTipoRegistro(),
                original.getIdRegistro(), 2, "{\"region\":\"Otra\"}", original.getAutor(), original.getCreadaEn()));
        VistaIntegridad alterado = consulta.integridad(TipoRegistro.HALLAZGO, H);
        assertFalse(alterado.integro());
        assertTrue(alterado.problemas().getFirst().contains("fueron alterados"));
    }

    @Test
    void integridadDetectaUnCambioBorrado() {
        registrado("e1");
        modificado("e2", "region", "Llanos", "A", 2, null, null);
        modificado("e3", "region", "A", "B", 3, null, null);
        almacen.cambios.remove(1); // se borra la versión 2

        VistaIntegridad r = consulta.integridad(TipoRegistro.HALLAZGO, H);
        assertFalse(r.integro());
        assertTrue(r.problemas().stream().anyMatch(p -> p.contains("cadena está rota")));
        assertTrue(r.problemas().stream().anyMatch(p -> p.contains("Versión 2: no está en el historial")));
    }

    @Test
    void registroSinHistorialDa404() {
        assertThrows(NoEncontradoException.class, () -> consulta.historial(TipoRegistro.HALLAZGO, "no-existe"));
        assertThrows(NoEncontradoException.class, () -> consulta.version(TipoRegistro.FICHA, "no-existe", 1));
    }
}
