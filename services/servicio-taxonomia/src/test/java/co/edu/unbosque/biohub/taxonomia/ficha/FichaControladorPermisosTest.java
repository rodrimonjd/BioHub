package co.edu.unbosque.biohub.taxonomia.ficha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.biohub.taxonomia.acceso.ClienteAutorizacion;
import co.edu.unbosque.biohub.taxonomia.acceso.VerificadorPermisos;
@WebMvcTest(FichaControlador.class)
@Import(VerificadorPermisos.class)
class FichaControladorPermisosTest {

    private static final String ATRIBUTO = """
            {"nombre":"pH del agua","valor":"6.8","autor":"suplantado"}""";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FichaServicio servicio;

    @MockitoBean
    private ClienteAutorizacion autorizacion;

    @Test
    void sinCabeceraNoSeAgreganAtributos() throws Exception {
        mvc.perform(post("/fichas/f-1/atributos").contentType(MediaType.APPLICATION_JSON).content(ATRIBUTO))
                .andExpect(status().isForbidden());
        verify(servicio, never()).incorporarAtributo(any(), any());
    }

    @Test
    void elInvitadoNoPuedeAgregarAtributos() throws Exception {
        when(autorizacion.tienePermiso("invitado", "ficha:editar")).thenReturn(false);
        mvc.perform(post("/fichas/f-1/atributos").header("X-Usuario-Id", "invitado")
                        .contentType(MediaType.APPLICATION_JSON).content(ATRIBUTO))
                .andExpect(status().isForbidden());
        verify(servicio, never()).incorporarAtributo(any(), any());
    }

    @Test
    void elInvitadoNoPuedeCambiarValores() throws Exception {
        when(autorizacion.tienePermiso("invitado", "ficha:editar")).thenReturn(false);
        mvc.perform(put("/fichas/f-1/atributos/pH").header("X-Usuario-Id", "invitado")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":\"7\"}"))
                .andExpect(status().isForbidden());
        verify(servicio, never()).modificarValor(any(), any(), any());
    }

    @Test
    void conPermisoElAutorEsElDeLaCabecera() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", "ficha:editar")).thenReturn(true);
        mvc.perform(post("/fichas/f-1/atributos").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON).content(ATRIBUTO))
                .andExpect(status().isOk());

        ArgumentCaptor<SolicitudesFicha.IncorporarAtributo> captor =
                ArgumentCaptor.forClass(SolicitudesFicha.IncorporarAtributo.class);
        verify(servicio).incorporarAtributo(eq("f-1"), captor.capture());
        assertThat(captor.getValue().autor()).isEqualTo("investigador.campo");
    }
}
