package co.edu.unbosque.biohub.gateway;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
public class ClienteAutorizacion {

    private final RestClient http;

    public ClienteAutorizacion(GatewayProperties propiedades) {
        this.http = RestClient.create(propiedades.autorizacionUrl());
    }

    public boolean validarConsumidor(String consumidorId) {
        RespuestaConsumidor respuesta = http.post()
                .uri("/api/v1/autorizacion/consumidores/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("consumidorId", consumidorId))
                .retrieve()
                .body(RespuestaConsumidor.class);
        return respuesta != null && respuesta.valido();
    }

    record RespuestaConsumidor(String consumidorId, boolean valido) { }
}
