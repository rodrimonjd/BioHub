package co.edu.unbosque.biohub.autorizacion.autorizacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.autorizacion.permiso.Permiso;
import co.edu.unbosque.biohub.autorizacion.rol.Rol;
import co.edu.unbosque.biohub.autorizacion.rol.RolRepositorio;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRol;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRolRepositorio;
import co.edu.unbosque.biohub.autorizacion.visibilidad.PoliticaVisibilidadUbicacion;

class ValidarConsumidorTest {

    private final UsuarioRolRepositorio usuarioRoles = mock(UsuarioRolRepositorio.class);
    private final RolRepositorio roles = mock(RolRepositorio.class);
    private final ServicioAutorizacion servicio =
            new ServicioAutorizacion(usuarioRoles, roles, new PoliticaVisibilidadUbicacion());

    private static Rol rolConPermisos(String nombre, String... codigos) {
        Rol rol = new Rol(nombre, null);
        Set<Permiso> permisos = new java.util.HashSet<>();
        for (String codigo : codigos) {
            Permiso permiso = mock(Permiso.class);
            when(permiso.getCodigo()).thenReturn(codigo);
            permisos.add(permiso);
        }
        rol.setPermisos(permisos);
        return rol;
    }

    @Test
    void consumidorConRolDeApiExternaEsValido() {
        Rol consumidor = rolConPermisos("CONSUMIDOR_EXTERNO", "hallazgo:ver", ServicioAutorizacion.PERMISO_API_EXTERNA);
        when(usuarioRoles.findByUsuarioId("consumidor-unal"))
                .thenReturn(List.of(new UsuarioRol("consumidor-unal", consumidor)));
        when(roles.findByNombreIn(any())).thenReturn(List.of(consumidor));

        assertTrue(servicio.validarConsumidor("consumidor-unal"));
    }

    @Test
    void consumidorSinRolCaeEnCiudadanoYNoEsValido() {
        Rol ciudadano = rolConPermisos("CIUDADANO", "hallazgo:ver");
        when(usuarioRoles.findByUsuarioId("consumidor-ong-suspendida")).thenReturn(List.of());
        when(roles.findByNombreIn(any())).thenReturn(List.of(ciudadano));

        assertFalse(servicio.validarConsumidor("consumidor-ong-suspendida"));
    }
}
