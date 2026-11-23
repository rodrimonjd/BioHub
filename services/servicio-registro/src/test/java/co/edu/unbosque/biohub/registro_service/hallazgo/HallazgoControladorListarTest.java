package co.edu.unbosque.biohub.registro_service.hallazgo;

import co.edu.unbosque.biohub.registro_service.acceso.ClienteAutorizacion;
import co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos;
import co.edu.unbosque.biohub.registro_service.especie.Especie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HallazgoControlador.class)
@Import(VerificadorPermisos.class)
class HallazgoControladorListarTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private HallazgoServicio servicio;

    @MockitoBean
    private ClienteAutorizacion autorizacion;

    private static Hallazgo hallazgo(String investigador, String departamento) {
        return new ConstructorHallazgo()
                .investigador(investigador)
                .especie(new Especie("Crocodylus intermedius", "Caimán", "CR"))
                .fecha(Instant.parse("2026-11-01T10:00:00Z"))
                .descripcion("Adulto en la orilla")
                .ubicacion(null, null, departamento, null)
                .build();
    }

    @Test
    void listaTodosLosHallazgosSinPedirCabecera() throws Exception {
        when(servicio.listar(null)).thenReturn(List.of(
                hallazgo("investigador.campo", "Meta"),
                hallazgo("admin", "Vichada")));

        mvc.perform(get("/hallazgos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].departamento").value("Meta"))
                .andExpect(jsonPath("$[1].investigadorId").value("admin"));
    }

    @Test
    void filtraPorInvestigador() throws Exception {
        when(servicio.listar("investigador.campo")).thenReturn(List.of(hallazgo("investigador.campo", "Meta")));

        mvc.perform(get("/hallazgos").param("investigadorId", "investigador.campo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].investigadorId").value("investigador.campo"));
    }
}
