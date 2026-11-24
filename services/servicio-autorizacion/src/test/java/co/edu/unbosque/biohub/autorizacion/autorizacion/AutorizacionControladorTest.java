package co.edu.unbosque.biohub.autorizacion.autorizacion;

import co.edu.unbosque.biohub.autorizacion.identidad.ServicioTokens;
import co.edu.unbosque.biohub.autorizacion.ubicacion.ServicioAccesoUbicacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutorizacionControlador.class)
class AutorizacionControladorTest {

    private static final String BASE = "/api/v1/autorizacion";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServicioAutorizacion autorizacion;

    @MockitoBean
    private ServicioAccesoUbicacion accesosUbicacion;

    @MockitoBean
    private ServicioTokens tokens;

    @Test
    void cadaCuentaSoloVeSusPropiosPermisos() throws Exception {
        when(autorizacion.permisosDe("inv")).thenReturn(Set.of("hallazgo:crear"));

        mvc.perform(get(BASE + "/usuarios/inv/permisos").header("X-Usuario-Id", "inv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos[0]").value("hallazgo:crear"));
        mvc.perform(get(BASE + "/usuarios/admin/permisos").header("X-Usuario-Id", "inv")).andExpect(status().isForbidden());
        mvc.perform(get(BASE + "/usuarios/admin/permisos")).andExpect(status().isForbidden());
    }

    @Test
    void elAdministradorYElInvitadoPuedenConsultarlos() throws Exception {
        when(autorizacion.verificar("admin", "roles:administrar")).thenReturn(true);

        mvc.perform(get(BASE + "/usuarios/inv/permisos").header("X-Usuario-Id", "admin")).andExpect(status().isOk());
        mvc.perform(get(BASE + "/usuarios/invitado/permisos")).andExpect(status().isOk());
    }

    @Test
    void elTokenDeUnConsumidorSoloSeEntregaAlServicioConElSecreto() throws Exception {
        when(autorizacion.validarConsumidor("consumidor-unal")).thenReturn(true);
        when(tokens.esServicioConfiable("secreto")).thenReturn(true);
        when(tokens.paraConsumidor("consumidor-unal")).thenReturn(new ServicioTokens.Token("t", Instant.now()));

        mvc.perform(post(BASE + "/consumidores/validar").header("X-Secreto-Servicio", "secreto")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"consumidorId\":\"consumidor-unal\"}"))
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.token").value("t"));
        mvc.perform(post(BASE + "/consumidores/validar")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"consumidorId\":\"consumidor-unal\"}"))
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.token").isEmpty());
    }

    @Test
    void unConsumidorSinPermisoNoRecibeToken() throws Exception {
        when(tokens.esServicioConfiable("secreto")).thenReturn(true);

        mvc.perform(post(BASE + "/consumidores/validar").header("X-Secreto-Servicio", "secreto")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"consumidorId\":\"suspendido\"}"))
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.token").isEmpty());
        verify(tokens, never()).paraConsumidor(any());
    }
}
