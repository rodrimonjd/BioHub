package co.edu.unbosque.biohub.reportes.reporte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.biohub.reportes.acceso.ClienteAutorizacion;
import co.edu.unbosque.biohub.reportes.acceso.VerificadorPermisos;

@WebMvcTest(ReporteControlador.class)
@Import(VerificadorPermisos.class)
class ReporteControladorPermisosTest {

    private static final String SOLICITUD = """
            {"fechaInicio":"2026-01-01","fechaFin":"2026-02-01","solicitadoPor":"suplantado"}""";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServicioReportes servicio;

    @MockitoBean
    private ClienteAutorizacion autorizacion;

    @Test
    void sinCabeceraDeUsuarioNoSeVenReportes() throws Exception {
        mvc.perform(get("/reportes")).andExpect(status().isForbidden());
        verify(servicio, never()).listar(any());
    }

    @Test
    void elCiudadanoSinReporteVerNoVeReportes() throws Exception {
        when(autorizacion.tienePermiso("invitado", VerificadorPermisos.VER_REPORTES)).thenReturn(false);
        mvc.perform(get("/reportes").header("X-Usuario-Id", "invitado")).andExpect(status().isForbidden());
        mvc.perform(get("/reportes/r1/exportar").header("X-Usuario-Id", "invitado")).andExpect(status().isForbidden());
        verify(servicio, never()).listar(any());
        verify(servicio, never()).paraExportar(any());
    }

    @Test
    void conReporteVerSeListanLosReportes() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", VerificadorPermisos.VER_REPORTES)).thenReturn(true);
        when(servicio.listar("investigador.campo")).thenReturn(List.of());
        mvc.perform(get("/reportes").param("solicitadoPor", "investigador.campo")
                        .header("X-Usuario-Id", "investigador.campo"))
                .andExpect(status().isOk());
    }

    @Test
    void sinReporteGenerarNoSePuedePedirUnReporte() throws Exception {
        when(autorizacion.tienePermiso("investigador.campo", VerificadorPermisos.GENERAR_REPORTES)).thenReturn(false);
        mvc.perform(post("/reportes").header("X-Usuario-Id", "investigador.campo")
                        .contentType(MediaType.APPLICATION_JSON).content(SOLICITUD))
                .andExpect(status().isForbidden());
        verify(servicio, never()).solicitar(any());
    }

    @Test
    void elSolicitanteEsElUsuarioDeLaCabeceraYNoElDelCuerpo() throws Exception {
        when(autorizacion.tienePermiso("autoridad.ambiental", VerificadorPermisos.GENERAR_REPORTES)).thenReturn(true);
        mvc.perform(post("/reportes").header("X-Usuario-Id", "autoridad.ambiental")
                        .contentType(MediaType.APPLICATION_JSON).content(SOLICITUD))
                .andExpect(status().isAccepted());
        ArgumentCaptor<SolicitudReporte> captor = ArgumentCaptor.forClass(SolicitudReporte.class);
        verify(servicio).solicitar(captor.capture());
        assertThat(captor.getValue().solicitadoPor()).isEqualTo("autoridad.ambiental");
    }
}
