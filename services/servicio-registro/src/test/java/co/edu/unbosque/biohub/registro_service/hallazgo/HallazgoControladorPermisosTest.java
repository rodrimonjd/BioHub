package co.edu.unbosque.biohub.registro_service.hallazgo;

import co.edu.unbosque.biohub.registro_service.acceso.ClienteAutorizacion;
import co.edu.unbosque.biohub.registro_service.acceso.VerificadorPermisos;
import co.edu.unbosque.biohub.registro_service.especie.Especie;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HallazgoControlador.class)
@Import(VerificadorPermisos.class)
class HallazgoControladorPermisosTest {

    private static final String CUERPO_HALLAZGO = """
            {"investigadorId":"suplantado","especieId":"11111111-1111-1111-1111-111111111111",
             "fecha":"2026-11-01T10:00:00Z","descripcion":"Adulto en la orilla","departamento":"Meta"}""";
    private static final UUID ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private HallazgoServicio servicio;

    @MockitoBean
    private ClienteAutorizacion autorizacion;

    private static Hallazgo hallazgo() {
        return new ConstructorHallazgo()
                .investigador("investigador.campo")
                .especie(new Especie("Crocodylus intermedius", "Caimán", "CR"))
                .fecha(Instant.parse("2026-11-01T10:00:00Z"))
                .descripcion("Adulto en la orilla")
                .ubicacion(null, null, "Meta", null)
                .build();
    }

    @Test
    void sinCabeceraNoSePuedeRegistrar() throws Exception {
        mvc.perform(post("/hallazgos").contentType(MediaType.APPLICATION_JSON).content(CUERPO_HALLAZGO))
                .andExpect(status().isForbidden());
        verify(servicio, never()).registrar(any());
    }

    @Test
    void ciudadanoSinPermisoNoPuedeRegistrar() throws Exception {
        when(autorizacion.tienePermiso("ciudadano", "hallazgo:crear")).thenReturn(false);
        mvc.perform(post("/hallazgos").header("X-Usuario-Id", "ciudadano")
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_HALLAZGO))
                .andExpect(status().isForbidden());
        verify(servicio, never()).registrar(any());
    }

    @Test
    void elInvestigadorSeTomaDeLaCabeceraNoDelCuerpo() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", "hallazgo:crear")).thenReturn(true);
        when(servicio.registrar(any())).thenReturn(hallazgo());

        mvc.perform(post("/hallazgos").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_HALLAZGO))
                .andExpect(status().isCreated());

        ArgumentCaptor<SolicitudHallazgo> enviado = ArgumentCaptor.forClass(SolicitudHallazgo.class);
        verify(servicio).registrar(enviado.capture());
        assertThat(enviado.getValue().investigadorId()).isEqualTo("investigador.campo");
    }

    @Test
    void modificarExigeHallazgoEditar() throws Exception {
        when(autorizacion.tienePermiso("ciudadano", "hallazgo:editar")).thenReturn(false);
        mvc.perform(patch("/hallazgos/" + ID).header("X-Usuario-Id", "ciudadano")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campo\":\"region\",\"valor\":\"Llanos\",\"autor\":\"otro\"}"))
                .andExpect(status().isForbidden());
        verify(servicio, never()).modificar(any(), any());
    }

    @Test
    void reclasificarExigeUnAutorizadorConHallazgoValidar() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", "hallazgo:editar")).thenReturn(true);
        when(autorizacion.tienePermiso("otro.investigador", "hallazgo:validar")).thenReturn(false);
        String cuerpo = "{\"nuevaEspecieId\":\"22222222-2222-2222-2222-222222222222\",\"motivo\":\"Revisión\","
                + "\"autorizador\":\"otro.investigador\"}";

        mvc.perform(post("/hallazgos/" + ID + "/reclasificacion").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        verify(servicio, never()).reclasificar(any(), any());
    }

    @Test
    void reclasificarConAutorizadorValidoUsaElAutorDeLaCabecera() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", "hallazgo:editar")).thenReturn(true);
        when(autorizacion.tienePermiso("autoridad.ambiental", "hallazgo:validar")).thenReturn(true);
        when(servicio.reclasificar(eq(ID), any())).thenReturn(hallazgo());
        String cuerpo = "{\"nuevaEspecieId\":\"22222222-2222-2222-2222-222222222222\",\"motivo\":\"Revisión\","
                + "\"autor\":\"suplantado\",\"autorizador\":\"autoridad.ambiental\"}";

        mvc.perform(post("/hallazgos/" + ID + "/reclasificacion").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk());

        ArgumentCaptor<SolicitudReclasificarHallazgo> enviado = ArgumentCaptor.forClass(SolicitudReclasificarHallazgo.class);
        verify(servicio).reclasificar(eq(ID), enviado.capture());
        assertThat(enviado.getValue().autor()).isEqualTo("investigador.campo");
    }

    @Test
    void siAutorizacionNoRespondeNoSeEscribe() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", "hallazgo:crear"))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "no disponible"));
        mvc.perform(post("/hallazgos").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_HALLAZGO))
                .andExpect(status().isServiceUnavailable());
        verify(servicio, never()).registrar(any());
    }
}
