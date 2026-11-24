package co.edu.unbosque.biohub.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

class ApiGatewayTest {

    private final ClienteAutorizacion autorizacion = mock(ClienteAutorizacion.class);

    private ApiGateway gateway(int limite) {
        GatewayProperties propiedades = new GatewayProperties(
                "http://localhost:8080", limite, "http://autorizacion",
                Map.of("/api/v1/inventario", "http://consulta/api/v1/consulta/hallazgos"),
                List.of(new GatewayProperties.Consumidor("consumidor-unal", "Universidad Nacional",
                        ConsumidorExterno.hash("clave-unal"))));
        return new ApiGateway(propiedades, autorizacion);
    }

    @Test
    void sinKeyOKeyInvalidaResponde401() {
        ApiGateway gateway = gateway(60);

        assertThat(gateway.enrutar("/api/v1/inventario", null, null).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(gateway.enrutar("/api/v1/inventario", null, "clave-falsa").getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(autorizacion, never()).validarConsumidor("consumidor-unal");
    }

    @Test
    void alSuperarElLimiteResponde429ConRetryAfter() {
        ApiGateway gateway = gateway(2);
        when(autorizacion.validarConsumidor("consumidor-unal")).thenReturn(true);

        gateway.enrutar("/api/v1/no-existe", null, "clave-unal");
        gateway.enrutar("/api/v1/no-existe", null, "clave-unal");
        ResponseEntity<String> tercera = gateway.enrutar("/api/v1/no-existe", null, "clave-unal");

        assertThat(tercera.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(tercera.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isNotNull();
        assertThat(tercera.getBody()).contains("2 solicitudes por minuto");
    }

    @Test
    void siAutorizacionLoRechazaResponde403() {
        when(autorizacion.validarConsumidor("consumidor-unal")).thenReturn(false);

        assertThat(gateway(60).enrutar("/api/v1/inventario", null, "clave-unal").getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void siAutorizacionNoRespondeResponde503() {
        when(autorizacion.validarConsumidor("consumidor-unal")).thenThrow(new ResourceAccessException("caido"));

        assertThat(gateway(60).enrutar("/api/v1/inventario", null, "clave-unal").getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void unaRutaQueNoExisteResponde404() {
        when(autorizacion.validarConsumidor("consumidor-unal")).thenReturn(true);

        assertThat(gateway(60).enrutar("/api/v1/otra-cosa", null, "clave-unal").getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void resuelveLaRutaPublicaAlServicioInterno() {
        ApiGateway gateway = gateway(60);

        assertThat(gateway.resolverDestino("/api/v1/inventario"))
                .isEqualTo("http://consulta/api/v1/consulta/hallazgos");
        assertThat(gateway.resolverDestino("/api/v1/inventario/abc-123"))
                .isEqualTo("http://consulta/api/v1/consulta/hallazgos/abc-123");
        assertThat(gateway.resolverDestino("/api/v1/inventarios")).isNull();
    }

    @Test
    void actualizarRutaCambiaElDestinoSinCambiarLaRutaPublica() {
        ApiGateway gateway = gateway(60);

        gateway.actualizarRuta("/api/v1/inventario", "http://consulta-nueva/hallazgos");

        assertThat(gateway.resolverDestino("/api/v1/inventario")).isEqualTo("http://consulta-nueva/hallazgos");
    }

    @Test
    void elResumenDeConsumoMuestraSolicitudesYRechazadasPorConsumidor() {
        ApiGateway gateway = gateway(2);
        when(autorizacion.validarConsumidor("consumidor-unal")).thenReturn(true);
        for (int i = 0; i < 3; i++) {
            gateway.enrutar("/api/v1/no-existe", null, "clave-unal");
        }

        List<ApiGateway.ConsumoConsumidor> consumo = gateway.consumo();

        assertThat(consumo).hasSize(1);
        assertThat(consumo.get(0).id()).isEqualTo("consumidor-unal");
        assertThat(consumo.get(0).solicitudes()).isEqualTo(2);
        assertThat(consumo.get(0).rechazadas()).isEqualTo(1);
        assertThat(consumo.get(0).limitePorMinuto()).isEqualTo(2);
    }
}
