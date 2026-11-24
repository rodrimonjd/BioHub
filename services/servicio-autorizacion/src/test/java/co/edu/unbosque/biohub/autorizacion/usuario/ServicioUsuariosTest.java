package co.edu.unbosque.biohub.autorizacion.usuario;

import co.edu.unbosque.biohub.autorizacion.autorizacion.ServicioAutorizacion;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.RespuestaSesion;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.SolicitudCrearUsuario;
import co.edu.unbosque.biohub.autorizacion.compartido.NoAutenticadoException;
import co.edu.unbosque.biohub.autorizacion.evento.EventoOutboxRepositorio;
import co.edu.unbosque.biohub.autorizacion.rol.RolServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicioUsuariosTest {

    private final Map<String, Usuario> tabla = new HashMap<>();
    private final UsuarioRepositorio repo = mock(UsuarioRepositorio.class);
    private final ServicioAutorizacion autorizacion = mock(ServicioAutorizacion.class);
    private final RolServicio roles = mock(RolServicio.class);
    private final PasswordEncoder claves = new BCryptPasswordEncoder(4);
    private ServicioUsuarios servicio;

    @BeforeEach
    void preparar() {
        when(repo.findById(anyString())).thenAnswer(i -> Optional.ofNullable(tabla.get(i.<String>getArgument(0))));
        when(repo.existsById(anyString())).thenAnswer(i -> tabla.containsKey(i.<String>getArgument(0)));
        when(repo.findByCorreoIgnoreCase(anyString())).thenAnswer(i -> tabla.values().stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(i.getArgument(0))).findFirst());
        when(repo.existsByCorreoIgnoreCase(anyString())).thenAnswer(i -> tabla.values().stream()
                .anyMatch(u -> u.getCorreo().equalsIgnoreCase(i.getArgument(0))));
        when(repo.save(any())).thenAnswer(i -> { Usuario u = i.getArgument(0); tabla.put(u.getId(), u); return u; });
        when(autorizacion.rolesDe("inv")).thenReturn(Set.of("INVESTIGADOR"));
        when(autorizacion.permisosDe("inv")).thenReturn(Set.of("hallazgo:crear", "historial:ver"));
        servicio = new ServicioUsuarios(repo, autorizacion, roles, mock(EventoOutboxRepositorio.class), claves,
                JsonMapper.builder().build());
        tabla.put("inv", new Usuario("inv", "Investigadora", "inv@biohub.gov.co", claves.encode("Clave-Segura-1")));
    }

    @Test
    void iniciaSesionConIdYDevuelveRolesYPermisos() {
        RespuestaSesion s = servicio.iniciarSesion("INV", "Clave-Segura-1");
        assertEquals("inv", s.usuarioId());
        assertEquals("Investigadora", s.nombre());
        assertEquals(Set.of("INVESTIGADOR"), s.roles());
        assertTrue(s.permisos().contains("hallazgo:crear"));
    }

    @Test
    void iniciaSesionConCorreoSinImportarMayusculas() {
        assertEquals("inv", servicio.iniciarSesion("INV@biohub.gov.co", "Clave-Segura-1").usuarioId());
    }

    @Test
    void claveIncorrectaYUsuarioInexistenteDanElMismoMensaje() {
        NoAutenticadoException mala = assertThrows(NoAutenticadoException.class,
                () -> servicio.iniciarSesion("inv", "otra-clave"));
        NoAutenticadoException noExiste = assertThrows(NoAutenticadoException.class,
                () -> servicio.iniciarSesion("nadie", "Clave-Segura-1"));
        assertEquals(ServicioUsuarios.CREDENCIALES_INVALIDAS, mala.getMessage());
        assertEquals(mala.getMessage(), noExiste.getMessage());
    }

    @Test
    void cuentaInactivaNoPuedeIniciarSesion() {
        ReflectionTestUtils.setField(tabla.get("inv"), "activo", false);
        assertThrows(NoAutenticadoException.class, () -> servicio.iniciarSesion("inv", "Clave-Segura-1"));
    }

    @Test
    void crearGuardaSoloElHashYAsignaElRol() {
        servicio.crear(new SolicitudCrearUsuario("Nueva.Persona", "Nueva Persona", "Nueva@BioHub.gov.co",
                "Clave-Nueva-2026", "AUTORIDAD_AMBIENTAL"), "admin");
        Usuario guardado = tabla.get("nueva.persona");
        assertEquals("nueva@biohub.gov.co", guardado.getCorreo());
        assertNotEquals("Clave-Nueva-2026", guardado.getHashClave());
        assertTrue(claves.matches("Clave-Nueva-2026", guardado.getHashClave()));
        verify(roles).asignarRol("nueva.persona", "AUTORIDAD_AMBIENTAL", "admin");
    }

    @Test
    void elIdDeInvitadoEstaReservado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(
                new SolicitudCrearUsuario("Invitado", "Alguien", "alguien@biohub.gov.co", "Clave-Segura-1", "ADMIN"), "admin"));
        verify(roles, never()).asignarRol(anyString(), anyString(), anyString());
    }

    @Test
    void noPermiteIdNiCorreoRepetidos() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(
                new SolicitudCrearUsuario("inv", "Otra", "otra@biohub.gov.co", "Clave-Segura-1", null), "admin"));
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(
                new SolicitudCrearUsuario("otra", "Otra", "INV@biohub.gov.co", "Clave-Segura-1", null), "admin"));
        verify(roles, never()).asignarRol(anyString(), anyString(), anyString());
    }
}
