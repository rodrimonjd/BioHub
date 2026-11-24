package co.edu.unbosque.biohub.gateway;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ClienteAutorizacion {

    private static final Duration DURACION_CACHE = Duration.ofSeconds(30);

    private final RestClient http;
    private final Map<String, PermisoEnCache> cachePermisos = new ConcurrentHashMap<>();

    public ClienteAutorizacion(GatewayProperties propiedades) {
        this.http = RestClient.create(propiedades.autorizacionUrl());
    }

    public boolean tienePermiso(String usuarioId, String permiso) {
        String clave = usuarioId + "|" + permiso;
        Instant ahora = Instant.now();
        PermisoEnCache enCache = cachePermisos.get(clave);
        if (enCache != null && enCache.vence().isAfter(ahora)) {
            return enCache.permitido();
        }
        RespuestaVerificacion respuesta = http.post()
                .uri("/api/v1/autorizacion/verificar")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("usuarioId", usuarioId, "permiso", permiso))
                .retrieve()
                .body(RespuestaVerificacion.class);
        boolean permitido = respuesta != null && respuesta.permitido();
        cachePermisos.put(clave, new PermisoEnCache(permitido, ahora.plus(DURACION_CACHE)));
        return permitido;
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

    record RespuestaVerificacion(String usuarioId, String permiso, boolean permitido) { }

    private record PermisoEnCache(boolean permitido, Instant vence) { }
}
