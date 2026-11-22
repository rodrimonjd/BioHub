package co.edu.unbosque.biohub.consulta.especie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class CatalogoEspeciesHttp implements CatalogoEspecies {

    private static final Logger log = LoggerFactory.getLogger(CatalogoEspeciesHttp.class);
    private static final Duration ESPERA_MINIMA_RECARGA = Duration.ofSeconds(10);

    private final RestClient cliente;
    private final Duration ttl;
    private final Clock reloj;

    private volatile Map<String, EspecieInfo> porId = Map.of();
    private volatile Map<String, EspecieInfo> porNombre = Map.of();
    private volatile Instant cargadoEn = Instant.EPOCH;
    private volatile Instant ultimoIntento = Instant.EPOCH;

    @Autowired
    public CatalogoEspeciesHttp(@Value("${consulta.registro-url}") String registroUrl,
                                @Value("${consulta.cache.especies-ttl}") Duration ttl) {
        this(crearCliente(registroUrl), ttl, Clock.systemUTC());
    }

    CatalogoEspeciesHttp(RestClient cliente, Duration ttl, Clock reloj) {
        this.cliente = cliente;
        this.ttl = ttl;
        this.reloj = reloj;
    }

    @Override
    public Optional<EspecieInfo> porId(String especieId) {
        if (especieId == null) return Optional.empty();
        return buscar(() -> porId.get(especieId));
    }

    @Override
    public Optional<EspecieInfo> porNombreCientifico(String nombreCientifico) {
        if (nombreCientifico == null) return Optional.empty();
        String clave = normalizar(nombreCientifico);
        return buscar(() -> porNombre.get(clave));
    }

    private Optional<EspecieInfo> buscar(java.util.function.Supplier<EspecieInfo> consulta) {
        Instant ahora = reloj.instant();
        boolean vencido = cargadoEn.plus(ttl).isBefore(ahora);
        EspecieInfo encontrada = vencido ? null : consulta.get();
        if (encontrada == null && ultimoIntento.plus(ESPERA_MINIMA_RECARGA).isBefore(ahora)) {
            recargar(ahora);
            encontrada = consulta.get();
        }
        return Optional.ofNullable(encontrada);
    }

    private synchronized void recargar(Instant ahora) {
        ultimoIntento = ahora;
        try {
            List<EspecieInfo> especies = cliente.get().uri("/especies")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EspecieInfo>>() {});
            Map<String, EspecieInfo> nuevoPorId = new HashMap<>();
            Map<String, EspecieInfo> nuevoPorNombre = new HashMap<>();
            for (EspecieInfo e : especies == null ? List.<EspecieInfo>of() : especies) {
                nuevoPorId.put(e.id(), e);
                nuevoPorNombre.put(normalizar(e.nombreCientifico()), e);
            }
            porId = Map.copyOf(nuevoPorId);
            porNombre = Map.copyOf(nuevoPorNombre);
            cargadoEn = ahora;
        } catch (RuntimeException e) {
            log.warn("No se pudo cargar el catálogo de especies de servicio-registro: {}", e.getMessage());
        }
    }

    private static String normalizar(String texto) {
        return texto.trim().toLowerCase(Locale.ROOT);
    }

    private static RestClient crearCliente(String baseUrl) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(3));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(fabrica).build();
    }
}
