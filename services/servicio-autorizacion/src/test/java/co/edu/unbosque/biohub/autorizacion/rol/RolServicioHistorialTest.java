package co.edu.unbosque.biohub.autorizacion.rol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import co.edu.unbosque.biohub.autorizacion.autorizacion.ServicioAutorizacion;
import co.edu.unbosque.biohub.autorizacion.compartido.ConflictoException;
import co.edu.unbosque.biohub.autorizacion.evento.EventoOutboxRepositorio;
import co.edu.unbosque.biohub.autorizacion.permiso.PermisoRepositorio;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRol;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRolRepositorio;
import tools.jackson.databind.json.JsonMapper;

class RolServicioHistorialTest {

    private final RolRepositorio roles = mock(RolRepositorio.class);
    private final UsuarioRolRepositorio usuarioRoles = mock(UsuarioRolRepositorio.class);
    private final CambioRolRepositorio historial = mock(CambioRolRepositorio.class);
    private final RolServicio servicio = new RolServicio(roles, mock(PermisoRepositorio.class), usuarioRoles,
            mock(EventoOutboxRepositorio.class), historial, mock(ServicioAutorizacion.class), JsonMapper.builder().build());

    private final Rol investigador = new Rol("INVESTIGADOR", "Investigador de campo");

    private CambioRol ultimoCambio() {
        ArgumentCaptor<CambioRol> captor = ArgumentCaptor.forClass(CambioRol.class);
        verify(historial).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void asignarUnRolQuedaEnElHistorialConQuienLoHizo() {
        when(roles.findByNombre("INVESTIGADOR")).thenReturn(Optional.of(investigador));
        when(usuarioRoles.findByUsuarioIdAndRolNombre("nueva.curadora", "INVESTIGADOR")).thenReturn(Optional.empty());

        servicio.asignarRol("nueva.curadora", "investigador", "admin");

        CambioRol cambio = ultimoCambio();
        assertThat(cambio.getAccion()).isEqualTo(CambioRol.Accion.ROL_ASIGNADO);
        assertThat(cambio.getUsuarioId()).isEqualTo("nueva.curadora");
        assertThat(cambio.getRol()).isEqualTo("INVESTIGADOR");
        assertThat(cambio.getRealizadoPor()).isEqualTo("admin");
    }

    @Test
    void quitarUnRolQuedaEnElHistorial() {
        when(usuarioRoles.findByUsuarioIdAndRolNombre("nueva.curadora", "INVESTIGADOR"))
                .thenReturn(Optional.of(new UsuarioRol("nueva.curadora", investigador)));

        servicio.revocarRol("nueva.curadora", "INVESTIGADOR", "admin");

        assertThat(ultimoCambio().getAccion()).isEqualTo(CambioRol.Accion.ROL_QUITADO);
    }

    @Test
    void unRolRepetidoNoSeAsignaNiQuedaEnElHistorial() {
        when(roles.findByNombre("INVESTIGADOR")).thenReturn(Optional.of(investigador));
        when(usuarioRoles.findByUsuarioIdAndRolNombre("nueva.curadora", "INVESTIGADOR"))
                .thenReturn(Optional.of(new UsuarioRol("nueva.curadora", investigador)));

        assertThatThrownBy(() -> servicio.asignarRol("nueva.curadora", "INVESTIGADOR", "admin"))
                .isInstanceOf(ConflictoException.class);
        verify(historial, never()).save(any());
    }

    @Test
    void elAdministradorNoPuedeQuitarseSuPropioRolDeAdmin() {
        assertThatThrownBy(() -> servicio.revocarRol("admin", "admin", "admin"))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("propio rol");
        verify(usuarioRoles, never()).delete(any());
        verify(historial, never()).save(any());
    }
}
