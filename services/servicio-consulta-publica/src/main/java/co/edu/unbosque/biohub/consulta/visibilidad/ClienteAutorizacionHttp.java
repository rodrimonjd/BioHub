package co.edu.unbosque.biohub.consulta.visibilidad;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import co.edu.unbosque.biohub.consulta.configuracion.PropiedadesConsulta;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClienteAutorizacionHttp implements ClienteAutorizacion {

    private static final Logger log = LoggerFactory.getLogger(ClienteAutorizacionHttp.class);
    private static final String ANONIMO = "";

    private final RestClient cliente;
    private final Cache<String, NivelVisibilidad> cache;

    public ClienteAutorizacionHttp(PropiedadesConsulta propiedades) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(3));
        this.cliente = RestClient.builder().baseUrl(propiedades.autorizacionUrl().toString()).requestFactory(fabrica).build();
        this.cache = Caffeine.newBuilder().expireAfterWrite(propiedades.cache().visibilidadTtl()).maximumSize(10_000).build();
    }

    @Override
    public NivelVisibilidad visibilidad(String usuarioId, String categoriaConservacion, String especieId) {
        String usuario = usuarioId == null || usuarioId.isBlank() ? ANONIMO : usuarioId.trim();
        String categoria = categoriaConservacion == null ? "" : categoriaConservacion.trim().toUpperCase();
        String especie = especieId == null ? "" : especieId.trim();
        String clave = usuario + "|" + categoria + "|" + especie;

        NivelVisibilidad enCache = cache.getIfPresent(clave);
        if (enCache != null) return enCache;
        try {
            NivelVisibilidad nivel = consultar(usuario, categoria, especie);
            cache.put(clave, nivel);
            return nivel;
        } catch (RuntimeException e) {
            log.warn("servicio-autorizacion no respondió ({}); se oculta la ubicación al nivel DEPARTAMENTO",
                    e.getMessage());
            return NivelVisibilidad.DEPARTAMENTO;
        }
    }

    private NivelVisibilidad consultar(String usuario, String categoria, String especie) {
        Map<String, String> cuerpo = new HashMap<>();
        cuerpo.put("usuarioId", usuario.isEmpty() ? null : usuario);
        cuerpo.put("categoriaConservacion", categoria.isEmpty() ? null : categoria);
        cuerpo.put("especieId", especie.isEmpty() ? null : especie);
        RespuestaVisibilidad respuesta = cliente.post()
                .uri("/api/v1/autorizacion/visibilidad-ubicacion")
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(RespuestaVisibilidad.class);
        if (respuesta == null || respuesta.nivel() == null) {
            throw new IllegalStateException("Respuesta vacía de servicio-autorizacion");
        }
        return respuesta.nivel();
    }

    record RespuestaVisibilidad(NivelVisibilidad nivel, boolean coordenadasExactas) {}
}
