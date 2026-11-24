package co.edu.unbosque.biohub.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

class GatewayControllerTest {

    private final ApiGateway gateway = mock(ApiGateway.class);
    private final ClienteAutorizacion autorizacion = mock(ClienteAutorizacion.class);
    private final GatewayController controlador = new GatewayController(gateway, autorizacion);

    private static HttpStatus estado(ResponseStatusException e) {
        return HttpStatus.valueOf(e.getStatusCode().value());
    }

    @Test
    void sinUsuarioNoSeMuestraElConsumo() {
        assertThatThrownBy(() -> controlador.consumo(null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(estado(e)).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void elCiudadanoSinPermisoNoVeElConsumo() {
        when(autorizacion.tienePermiso("invitado", GatewayController.PERMISO_MONITOREO)).thenReturn(false);
        assertThatThrownBy(() -> controlador.consumo("invitado"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(estado(e)).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void conPermisoSeMuestraElConsumo() {
        when(autorizacion.tienePermiso("admin", GatewayController.PERMISO_MONITOREO)).thenReturn(true);
        when(gateway.getUrlPublica()).thenReturn("http://localhost:8080");
        when(gateway.consumo()).thenReturn(List.of());
        assertThat(controlador.consumo("admin").urlPublica()).isEqualTo("http://localhost:8080");
    }

    @Test
    void siAutorizacionNoRespondeElConsumoNoSeEntrega() {
        when(autorizacion.tienePermiso("admin", GatewayController.PERMISO_MONITOREO))
                .thenThrow(new ResourceAccessException("caido"));
        assertThatThrownBy(() -> controlador.consumo("admin"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(estado(e)).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }
}
