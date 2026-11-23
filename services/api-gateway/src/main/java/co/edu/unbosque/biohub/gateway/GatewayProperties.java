package co.edu.unbosque.biohub.gateway;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("biohub.gateway")
public record GatewayProperties(
        String urlPublica,
        int limitePorMinuto,
        String autorizacionUrl,
        Map<String, String> rutas,
        List<Consumidor> consumidores) {

    public record Consumidor(String id, String organizacion, String hashApiKey) { }
}
