package co.edu.unbosque.biohub.gateway;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;


@Service
public class ApiGateway {

    public static final String CABECERA_API_KEY = "X-API-Key";
    public static final String CABECERA_USUARIO = "X-Usuario-Id";

    private final String urlPublica;
    private final Map<String, String> rutas = new LinkedHashMap<>();
    private final List<ConsumidorExterno> consumidores = new ArrayList<>();
    private final ClienteAutorizacion autorizacion;
    private final RestClient http = RestClient.create();

    public ApiGateway(GatewayProperties propiedades, ClienteAutorizacion autorizacion) {
        this.urlPublica = propiedades.urlPublica();
        this.autorizacion = autorizacion;
        for (Map.Entry<String, String> ruta : propiedades.rutas().entrySet()) {
            actualizarRuta(ruta.getKey(), ruta.getValue());
        }
        for (GatewayProperties.Consumidor c : propiedades.consumidores()) {
            consumidores.add(new ConsumidorExterno(c.id(), c.organizacion(), c.hashApiKey(),
                    propiedades.limitePorMinuto()));
        }
    }

    public ResponseEntity<String> enrutar(String ruta, String consulta, String credencial) {
        ConsumidorExterno consumidor = autenticar(credencial);
        if (consumidor == null) {
            return error(HttpStatus.UNAUTHORIZED, "API key ausente o invalida (cabecera " + CABECERA_API_KEY + ")");
        }

        if (!consumidor.registrarSolicitud()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(ConsumidorExterno.segundosParaSiguienteMinuto(Instant.now())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpoError(HttpStatus.TOO_MANY_REQUESTS, "Supero el limite de " + consumidor.getLimitePorMinuto() + " solicitudes por minuto"));
        }

        try {
            if (!autorizacion.validarConsumidor(consumidor.getId())) {
                return error(HttpStatus.FORBIDDEN, "El consumidor no tiene permiso para usar la API externa");
            }
        } catch (RestClientException e) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de autorizacion no responde");
        }

        String destino = resolverDestino(ruta);
        if (destino == null) {
            return error(HttpStatus.NOT_FOUND, "Ruta no disponible en la API publica: " + ruta);
        }
        if (consulta != null && !consulta.isBlank()) {
            destino = destino + "?" + consulta;
        }
        return reenviar(destino, consumidor.getId());
    }

    public void actualizarRuta(String ruta, String destino) {
        rutas.put(ruta, destino);
    }

    String resolverDestino(String ruta) {
        for (Map.Entry<String, String> entrada : rutas.entrySet()) {
            String publica = entrada.getKey();
            if (ruta.equals(publica)) {
                return entrada.getValue();
            }
            if (ruta.startsWith(publica + "/")) {
                return entrada.getValue() + ruta.substring(publica.length());
            }
        }
        return null;
    }

    ConsumidorExterno autenticar(String apiKey) {
        for (ConsumidorExterno consumidor : consumidores) {
            if (consumidor.autenticar(apiKey)) {
                return consumidor;
            }
        }
        return null;
    }

    private ResponseEntity<String> reenviar(String destino, String consumidorId) {
        try {
            return http.get()
                    .uri(destino)
                    .header(CABECERA_USUARIO, consumidorId)
                    .exchange((solicitud, respuesta) -> ResponseEntity.status(respuesta.getStatusCode())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new String(respuesta.getBody().readAllBytes())));
        } catch (RestClientException e) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "El servicio interno no responde");
        }
    }

    private static ResponseEntity<String> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpoError(estado, mensaje));
    }

    private static String cuerpoError(HttpStatus estado, String mensaje) {
        return "{\"estado\":" + estado.value()
                + ",\"error\":\"" + estado.getReasonPhrase()
                + "\",\"mensaje\":\"" + mensaje.replace("\"", "'") + "\"}";
    }

    public String getUrlPublica() { return urlPublica; }
}
