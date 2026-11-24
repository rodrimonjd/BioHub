package co.edu.unbosque.biohub.registro_service.especie;

import co.edu.unbosque.biohub.registro_service.acceso.ClienteAutorizacion;
import co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EspecieControlador.class)
@Import(VerificadorPermisos.class)
class EspecieControladorTest {

    private static final String CUERPO = """
            {"nombreCientifico":"Tremarctos ornatus","nombreComun":"Oso de anteojos","estadoConservacion":"VU"}""";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EspecieRepositorio especies;

    @MockitoBean
    private EspecieServicio servicio;

    @MockitoBean
    private ClienteAutorizacion autorizacion;

    @Test
    void sinPermisoNoSePuedeCrear() throws Exception {
        when(autorizacion.tienePermiso("invitado", "hallazgo:crear")).thenReturn(false);
        mvc.perform(post("/especies").header("X-Usuario-Id", "invitado")
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden());
        verify(servicio, never()).crear(any());
    }

    @Test
    void conPermisoSeCreaYDevuelve201() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", "hallazgo:crear")).thenReturn(true);
        Especie creada = new Especie("Tremarctos ornatus", "Oso de anteojos", "VU");
        when(servicio.crear(any())).thenReturn(creada);

        mvc.perform(post("/especies").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/especies/" + creada.getId()))
                .andExpect(jsonPath("$.nombreCientifico").value("Tremarctos ornatus"))
                .andExpect(jsonPath("$.estadoConservacion").value("VU"));
    }

    @Test
    void categoriaUicnInvalidaEs400() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", "hallazgo:crear")).thenReturn(true);
        mvc.perform(post("/especies").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCientifico\":\"Tremarctos ornatus\",\"estadoConservacion\":\"XX\"}"))
                .andExpect(status().isBadRequest());
        verify(servicio, never()).crear(any());
    }

    @Test
    void nombreRepetidoEs409() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", "hallazgo:crear")).thenReturn(true);
        when(servicio.crear(any())).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe"));
        mvc.perform(post("/especies").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isConflict());
    }
}
