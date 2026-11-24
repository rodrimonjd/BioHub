package co.edu.unbosque.biohub.registro_service.acceso;

import co.edu.unbosque.biohub.registro_service.configuracion.PropiedadesRegistro;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Component
public class ClienteAutorizacionHttp implements ClienteAutorizacion {

    private static final Logger log = LoggerFactory.getLogger(ClienteAutorizacionHttp.class);

    private final RestClient cliente;
    private final Cache<String, Boolean> cache;
    private final Cache<String, Set<String>> cacheEspecies;

    public ClienteAutorizacionHttp(PropiedadesRegistro propiedades) {
        PropiedadesRegistro.Autorizacion config = propiedades.autorizacion();
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(3));
        this.cliente = RestClient.builder().baseUrl(config.url().toString()).requestFactory(fabrica).build();
        this.cache = Caffeine.newBuilder().expireAfterWrite(config.cacheTtl()).maximumSize(10_000).build();
        this.cacheEspecies = Caffeine.newBuilder().expireAfterWrite(config.cacheTtl()).maximumSize(10_000).build();
    }

    @Override
    public boolean tienePermiso(String usuarioId, String permiso) {
        String clave = usuarioId + "|" + permiso;
        Boolean enCache = cache.getIfPresent(clave);
        if (enCache != null) return enCache;
        try {
            RespuestaVerificacion respuesta = cliente.post()
                    .uri("/api/v1/autorizacion/verificar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("usuarioId", usuarioId, "permiso", permiso))
                    .retrieve()
                    .body(RespuestaVerificacion.class);
            boolean permitido = respuesta != null && respuesta.permitido();
            cache.put(clave, permitido);
            return permitido;
        } catch (RuntimeException e) {
            log.warn("servicio-autorizacion no respondió al verificar {} para {}: {}", permiso, usuarioId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo verificar el permiso: servicio de autorización no disponible");
        }
    }

    @Override
    public Set<String> especiesConUbicacionExacta(String usuarioId) {
        Set<String> enCache = cacheEspecies.getIfPresent(usuarioId);
        if (enCache != null) return enCache;
        try {
            RespuestaAccesoUbicacion respuesta = cliente.get()
                    .uri("/api/v1/autorizacion/usuarios/{usuarioId}/ubicacion", usuarioId)
                    .retrieve()
                    .body(RespuestaAccesoUbicacion.class);
            Set<String> especies = respuesta == null || respuesta.especies() == null
                    ? Set.of() : Set.copyOf(respuesta.especies());
            cacheEspecies.put(usuarioId, especies);
            return especies;
        } catch (RuntimeException e) {
            log.warn("servicio-autorizacion no respondió al consultar las ubicaciones de {}: {}", usuarioId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo verificar el acceso a ubicaciones: servicio de autorización no disponible");
        }
    }

    record RespuestaVerificacion(String usuarioId, String permiso, boolean permitido) {}

    record RespuestaAccesoUbicacion(String usuarioId, boolean todas, Set<String> especies) {}
}
