package co.edu.unbosque.biohub.autorizacion.usuario;

import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.RespuestaSesion;
import co.edu.unbosque.biohub.autorizacion.compartido.NoAutenticadoException;
import co.edu.unbosque.biohub.autorizacion.compartido.ProhibidoException;
import co.edu.unbosque.biohub.autorizacion.rol.RolServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioControlador.class)
class UsuarioControladorTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ServicioUsuarios servicio;

    @MockitoBean
    private RolServicio roles;

    @Test
    void sesionCorrectaDevuelvePermisosSinLaClave() throws Exception {
        when(servicio.iniciarSesion("inv", "Clave-Segura-1")).thenReturn(new RespuestaSesion("inv", "Investigadora",
                "inv@biohub.gov.co", Set.of("INVESTIGADOR"), Set.of("hallazgo:crear")));

        mvc.perform(post("/api/v1/autorizacion/sesiones").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"inv\",\"clave\":\"Clave-Segura-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value("inv"))
                .andExpect(jsonPath("$.permisos[0]").value("hallazgo:crear"))
                .andExpect(content().string(not(containsString("Clave-Segura-1"))));
    }

    @Test
    void sesionIncorrectaDa401() throws Exception {
        when(servicio.iniciarSesion("inv", "mala")).thenThrow(new NoAutenticadoException("Usuario o contraseña incorrectos"));

        mvc.perform(post("/api/v1/autorizacion/sesiones").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"inv\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void sesionSinClaveDa400() throws Exception {
        mvc.perform(post("/api/v1/autorizacion/sesiones").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"inv\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("clave: es obligatoria"));
    }

    @Test
    void administrarCuentasExigeAdmin() throws Exception {
        doThrow(new ProhibidoException("Se requiere el permiso roles:administrar")).when(roles).exigirAdmin("inv");

        mvc.perform(get("/api/v1/autorizacion/usuarios").header("X-Usuario-Id", "inv"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/autorizacion/usuarios").header("X-Usuario-Id", "inv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"x.y\",\"nombre\":\"X\",\"correo\":\"x@y.co\",\"clave\":\"Clave-Segura-1\"}"))
                .andExpect(status().isForbidden());
        verify(servicio, never()).crear(any());
    }

    @Test
    void listarAutorizadoresExigeUnaCuentaActiva() throws Exception {
        when(servicio.esCuentaActiva("inv")).thenReturn(true);
        when(servicio.conPermiso("hallazgo:validar")).thenReturn(
                java.util.List.of(new co.edu.unbosque.biohub.autorizacion.compartido.Dtos.ResumenUsuario(
                        "autoridad.ambiental", "Autoridad ambiental")));

        mvc.perform(get("/api/v1/autorizacion/usuarios/con-permiso/hallazgo:validar").header("X-Usuario-Id", "inv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("autoridad.ambiental"))
                .andExpect(jsonPath("$[0].correo").doesNotExist());
        mvc.perform(get("/api/v1/autorizacion/usuarios/con-permiso/hallazgo:validar"))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearCuentaValidaLosDatos() throws Exception {
        mvc.perform(post("/api/v1/autorizacion/usuarios").header("X-Usuario-Id", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"x y\",\"nombre\":\"X\",\"correo\":\"no-es-correo\",\"clave\":\"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("clave: debe tener entre 8 y 72 caracteres")))
                .andExpect(jsonPath("$.mensaje").value(containsString("correo: no es un correo válido")));
    }
}
