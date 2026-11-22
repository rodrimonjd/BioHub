package co.edu.unbosque.biohub.consulta.consulta;

import co.edu.unbosque.biohub.consulta.compartido.NoEncontradoException;
import co.edu.unbosque.biohub.consulta.visibilidad.NivelVisibilidad;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsultaControlador.class)
class ConsultaControladorTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServicioConsulta servicio;

    private static final VistaHallazgoPublico VISTA = new VistaHallazgoPublico("h1", "e1", "Crocodylus intermedius",
            "Caimán del Orinoco", "CR", Instant.parse("2026-11-01T10:00:00Z"), "Adulto", "Meta", null, null, null,
            NivelVisibilidad.DEPARTAMENTO);

    @Test
    void listaHallazgosComoAnonimo() throws Exception {
        when(servicio.listar(isNull(), isNull(), eq("Meta"), isNull(), eq(0), eq(20)))
                .thenReturn(new PaginaHallazgos(List.of(VISTA), 0, 20, 1, 1));

        mvc.perform(get("/api/v1/consulta/hallazgos").param("departamento", "Meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].nivelUbicacion").value("DEPARTAMENTO"))
                .andExpect(jsonPath("$.contenido[0].latitud").isEmpty());
    }

    @Test
    void reenviaElUsuarioDeLaCabecera() throws Exception {
        when(servicio.obtener("investigador-1", "h1")).thenReturn(VISTA);

        mvc.perform(get("/api/v1/consulta/hallazgos/h1").header("X-Usuario-Id", "investigador-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("h1"));
    }

    @Test
    void hallazgoInexistenteDa404() throws Exception {
        when(servicio.obtener(isNull(), eq("nada"))).thenThrow(new NoEncontradoException("Hallazgo no encontrado: nada"));

        mvc.perform(get("/api/v1/consulta/hallazgos/nada"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Hallazgo no encontrado: nada"));
    }

    @Test
    void parametrosInvalidosDan400() throws Exception {
        when(servicio.listar(isNull(), isNull(), isNull(), isNull(), eq(0), anyInt()))
                .thenThrow(new IllegalArgumentException("tamano debe estar entre 1 y 100"));

        mvc.perform(get("/api/v1/consulta/hallazgos").param("tamano", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("tamano debe estar entre 1 y 100"));

        mvc.perform(get("/api/v1/consulta/hallazgos").param("pagina", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Valor inválido para el parámetro pagina"));
    }
}
