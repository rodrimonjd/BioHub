package co.edu.unbosque.biohub.historial.historial;

import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;
import co.edu.unbosque.biohub.historial.compartido.NoEncontradoException;
import co.edu.unbosque.biohub.historial.compartido.ServicioNoDisponibleException;
import co.edu.unbosque.biohub.historial.historial.Vistas.VistaHistorial;
import co.edu.unbosque.biohub.historial.permiso.ClienteAutorizacion;
import co.edu.unbosque.biohub.historial.configuracion.PropiedadesHistorial;
import co.edu.unbosque.biohub.historial.permiso.VerificadorPermisos;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistorialControlador.class)
@Import(VerificadorPermisos.class)
@EnableConfigurationProperties(PropiedadesHistorial.class)
@TestPropertySource(properties = "historial.permiso-consulta=historial:ver")
class HistorialControladorTest {

    private static final String URL = "/api/v1/historial/hallazgos/h1";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServicioConsultaHistorial servicio;

    @MockitoBean
    private ClienteAutorizacion autorizacion;

    @Test
    void conPermisoDevuelveElHistorial() throws Exception {
        when(autorizacion.tienePermiso("auditor-1", "historial:ver")).thenReturn(true);
        when(servicio.historial(TipoRegistro.HALLAZGO, "h1")).thenReturn(new VistaHistorial(TipoRegistro.HALLAZGO,
                "h1", 1, 0, true, List.of(), "El registro no ha sido modificado", List.of()));

        mvc.perform(get(URL).header("X-Usuario-Id", "auditor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoRegistro").value("HALLAZGO"))
                .andExpect(jsonPath("$.mensaje").value("El registro no ha sido modificado"));
    }

    @Test
    void sinCabeceraDeUsuarioDa403() throws Exception {
        mvc.perform(get(URL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Se requiere la cabecera X-Usuario-Id con el permiso historial:ver"));
        verify(servicio, never()).historial(any(), any());
    }

    @Test
    void sinElPermisoDa403() throws Exception {
        when(autorizacion.tienePermiso("ciudadano", "historial:ver")).thenReturn(false);
        mvc.perform(get(URL + "/versiones").header("X-Usuario-Id", "ciudadano"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Se requiere el permiso historial:ver"));
    }

    @Test
    void siAutorizacionNoRespondeDa503() throws Exception {
        when(autorizacion.tienePermiso("auditor-1", "historial:ver"))
                .thenThrow(new ServicioNoDisponibleException("No se pudo verificar el permiso"));
        mvc.perform(get(URL + "/integridad").header("X-Usuario-Id", "auditor-1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void historialInexistenteDa404() throws Exception {
        when(autorizacion.tienePermiso("auditor-1", "historial:ver")).thenReturn(true);
        when(servicio.historial(TipoRegistro.FICHA, "nada")).thenThrow(new NoEncontradoException("No hay historial"));
        mvc.perform(get("/api/v1/historial/fichas/nada").header("X-Usuario-Id", "auditor-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void tipoDeRegistroDesconocidoYEscrituraNoExisten() throws Exception {
        mvc.perform(get("/api/v1/historial/avistamientos/x").header("X-Usuario-Id", "auditor-1"))
                .andExpect(status().isNotFound());
        mvc.perform(post(URL).header("X-Usuario-Id", "auditor-1"))
                .andExpect(status().isMethodNotAllowed());
    }
}
