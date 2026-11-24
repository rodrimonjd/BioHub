package co.edu.unbosque.biohub.autorizacion.ubicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import co.edu.unbosque.biohub.autorizacion.autorizacion.ServicioAutorizacion;
import co.edu.unbosque.biohub.autorizacion.compartido.ConflictoException;
import co.edu.unbosque.biohub.autorizacion.compartido.NoEncontradoException;
import co.edu.unbosque.biohub.autorizacion.compartido.ProhibidoException;
import co.edu.unbosque.biohub.autorizacion.ubicacion.CambioAccesoUbicacion.Accion;
import co.edu.unbosque.biohub.autorizacion.usuario.Usuario;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRepositorio;

class ServicioAccesoUbicacionTest {

    private static final String JAGUAR = "22222222-2222-2222-2222-222222222222";

    private final AccesoUbicacionRepositorio accesos = mock(AccesoUbicacionRepositorio.class);
    private final CambioAccesoUbicacionRepositorio historial = mock(CambioAccesoUbicacionRepositorio.class);
    private final UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    private final ServicioAutorizacion autorizacion = mock(ServicioAutorizacion.class);
    private final ServicioAccesoUbicacion servicio =
            new ServicioAccesoUbicacion(accesos, historial, usuarios, autorizacion);

    @BeforeEach
    void cuentas() {
        Usuario investigador = mock(Usuario.class);
        when(investigador.getId()).thenReturn("investigador.campo");
        when(investigador.getNombre()).thenReturn("Investigador de campo");
        when(investigador.isActivo()).thenReturn(true);
        when(usuarios.findById("investigador.campo")).thenReturn(Optional.of(investigador));
        when(usuarios.findAllByOrderByIdAsc()).thenReturn(List.of(investigador));
        when(autorizacion.verificar("autoridad.ambiental", ServicioAccesoUbicacion.PERMISO_ADMINISTRAR)).thenReturn(true);
        when(accesos.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void sinUbicacionAdministrarNoSePuedeDarAcceso() {
        assertThatThrownBy(() -> servicio.otorgar("investigador.campo", "investigador.campo", JAGUAR))
                .isInstanceOf(ProhibidoException.class);
        verify(accesos, never()).save(any());
        verify(historial, never()).save(any());
    }

    @Test
    void darAccesoLoGuardaYQuedaEnElHistorial() {
        when(accesos.findByUsuarioIdAndEspecieId("investigador.campo", JAGUAR)).thenReturn(Optional.empty());

        ServicioAccesoUbicacion.VistaAcceso vista = servicio.otorgar("autoridad.ambiental", "Investigador.Campo", JAGUAR);

        assertThat(vista.usuarioId()).isEqualTo("investigador.campo");
        assertThat(vista.otorgadoPor()).isEqualTo("autoridad.ambiental");
        ArgumentCaptor<CambioAccesoUbicacion> cambio = ArgumentCaptor.forClass(CambioAccesoUbicacion.class);
        verify(historial).save(cambio.capture());
        assertThat(cambio.getValue().getAccion()).isEqualTo(Accion.OTORGADO);
        assertThat(cambio.getValue().getRealizadoPor()).isEqualTo("autoridad.ambiental");
        assertThat(cambio.getValue().getEspecieId()).isEqualTo(JAGUAR);
    }

    @Test
    void noSeDaUnAccesoRepetido() {
        when(accesos.findByUsuarioIdAndEspecieId("investigador.campo", JAGUAR))
                .thenReturn(Optional.of(new AccesoUbicacion("investigador.campo", JAGUAR, "admin")));
        assertThatThrownBy(() -> servicio.otorgar("autoridad.ambiental", "investigador.campo", JAGUAR))
                .isInstanceOf(ConflictoException.class);
        verify(historial, never()).save(any());
    }

    @Test
    void quienYaVeTodasLasEspeciesNoNecesitaAcceso() {
        when(autorizacion.verificar("investigador.campo", "ubicacion:ver_exacta")).thenReturn(true);
        assertThatThrownBy(() -> servicio.otorgar("autoridad.ambiental", "investigador.campo", JAGUAR))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void laEspecieDebeSerUnIdentificadorValido() {
        assertThatThrownBy(() -> servicio.otorgar("autoridad.ambiental", "investigador.campo", "jaguar"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void quitarAccesoLoBorraYQuedaEnElHistorial() {
        AccesoUbicacion acceso = new AccesoUbicacion("investigador.campo", JAGUAR, "autoridad.ambiental");
        when(accesos.findByUsuarioIdAndEspecieId("investigador.campo", JAGUAR)).thenReturn(Optional.of(acceso));

        servicio.revocar("autoridad.ambiental", "investigador.campo", JAGUAR);

        verify(accesos).delete(acceso);
        ArgumentCaptor<CambioAccesoUbicacion> cambio = ArgumentCaptor.forClass(CambioAccesoUbicacion.class);
        verify(historial).save(cambio.capture());
        assertThat(cambio.getValue().getAccion()).isEqualTo(Accion.REVOCADO);
    }

    @Test
    void quitarUnAccesoQueNoExisteResponde404() {
        when(accesos.findByUsuarioIdAndEspecieId("investigador.campo", JAGUAR)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.revocar("autoridad.ambiental", "investigador.campo", JAGUAR))
                .isInstanceOf(NoEncontradoException.class);
        verify(historial, never()).save(any());
    }

    @Test
    void elAccesoDeUnUsuarioListaSusEspecies() {
        when(accesos.findByUsuarioId("investigador.campo"))
                .thenReturn(List.of(new AccesoUbicacion("investigador.campo", JAGUAR, "autoridad.ambiental")));

        ServicioAccesoUbicacion.AccesoDeUsuario acceso = servicio.accesoDe("investigador.campo");

        assertThat(acceso.todas()).isFalse();
        assertThat(acceso.especies()).containsExactly(JAGUAR);
        assertThat(servicio.accesoDe(null).especies()).isEmpty();
    }
}
