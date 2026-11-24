package co.edu.unbosque.biohub.autorizacion.compartido;

import co.edu.unbosque.biohub.autorizacion.autorizacion.AutorizacionControlador;
import co.edu.unbosque.biohub.autorizacion.autorizacion.ServicioAutorizacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutorizacionControlador.class)
class ManejadorExcepcionesTest {

    private static final String VERIFICAR = "/api/v1/autorizacion/verificar";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServicioAutorizacion autorizacion;

    @MockitoBean
    private co.edu.unbosque.biohub.autorizacion.ubicacion.ServicioAccesoUbicacion accesosUbicacion;

    @Test
    void validacionDevuelveSoloCampoYMensaje() throws Exception {
        mvc.perform(post(VERIFICAR).contentType(MediaType.APPLICATION_JSON).content("{\"usuarioId\":\"admin\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("permiso: es obligatorio"))
                .andExpect(content().string(not(containsString("co.edu"))));
    }

    @Test
    void variosCamposInvalidosSeListanJuntos() throws Exception {
        mvc.perform(post(VERIFICAR).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("permiso: es obligatorio; usuarioId: es obligatorio"));
    }

    @Test
    void jsonMalformadoUsaElFormatoPropio() throws Exception {
        mvc.perform(post(VERIFICAR).contentType(MediaType.APPLICATION_JSON).content("{mal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la solicitud falta o no es un JSON válido"));
    }

    @Test
    void metodoNoPermitidoUsaElFormatoPropio() throws Exception {
        mvc.perform(get(VERIFICAR))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.estado").value(405))
                .andExpect(jsonPath("$.mensaje").value("Método HTTP no permitido para esta ruta"));
    }

    @Test
    void errorInesperadoNoExponeDetalles() throws Exception {
        when(autorizacion.verificar(any(), any())).thenThrow(new IllegalStateException("detalle interno secreto"));

        mvc.perform(post(VERIFICAR).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":\"admin\",\"permiso\":\"hallazgo:ver\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"))
                .andExpect(content().string(not(containsString("secreto"))));
    }
}
