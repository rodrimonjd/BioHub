package co.edu.unbosque.biohub.registro_service.hallazgo;

import co.edu.unbosque.biohub.registro_service.acceso.ClienteAutorizacion;
import co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos;
import co.edu.unbosque.biohub.registro_service.especie.Especie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HallazgoControlador.class)
@Import(VerificadorPermisos.class)
class HallazgoControladorEvidenciaTest {

    private static final UUID ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final String EVIDENCIA = """
            {"tipo":"ADN","valor":"ADN-2026-901"}""";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private HallazgoServicio servicio;

    @MockitoBean
    private ClienteAutorizacion autorizacion;

    @Test
    void sinPermisoDeEdicionNoSeAgregaEvidencia() throws Exception {
        when(autorizacion.tienePermiso("autoridad.ambiental", "hallazgo:editar")).thenReturn(false);

        mvc.perform(post("/hallazgos/" + ID + "/evidencias").header("X-Usuario-Id", "autoridad.ambiental")
                        .contentType(MediaType.APPLICATION_JSON).content(EVIDENCIA))
                .andExpect(status().isForbidden());
        verify(servicio, never()).agregarEvidencia(any(), any(), any());
    }

    @Test
    void elInvestigadorAgregaEvidenciaConSuIdComoAutor() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", "hallazgo:editar")).thenReturn(true);
        Hallazgo hallazgo = new ConstructorHallazgo()
                .investigador("investigador.campo")
                .especie(new Especie("Crocodylus intermedius", "Caimán", "CR"))
                .fecha(Instant.parse("2026-11-01T10:00:00Z"))
                .descripcion("Adulto en la orilla")
                .ubicacion(null, null, "Meta", null)
                .build();
        when(servicio.agregarEvidencia(eq(ID), any(), eq("investigador.campo"))).thenReturn(hallazgo);

        mvc.perform(post("/hallazgos/" + ID + "/evidencias").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON).content(EVIDENCIA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investigadorId").value("investigador.campo"));
    }
}
