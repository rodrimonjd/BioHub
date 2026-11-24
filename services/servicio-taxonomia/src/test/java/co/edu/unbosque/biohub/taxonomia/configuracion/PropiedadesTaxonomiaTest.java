package co.edu.unbosque.biohub.taxonomia.configuracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/** La configuración se enlaza con tipos y se valida al arrancar: un valor faltante o inválido impide iniciar. */
class PropiedadesTaxonomiaTest {

    @Configuration
    @EnableConfigurationProperties(PropiedadesTaxonomia.class)
    static class Config {
    }

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(Config.class)
            .withPropertyValues(
                    "biohub.eventos.exchange=biohub.eventos",
                    "biohub.eventos.intervalo-ms=5000",
                    "biohub.autorizacion.url=http://localhost:8082",
                    "biohub.autorizacion.cache-ttl=60s");

    @Test
    void enlazaLosValoresConSuTipo() {
        contexto.run(ctx -> {
            PropiedadesTaxonomia p = ctx.getBean(PropiedadesTaxonomia.class);
            assertThat(p.eventos().intervaloMs()).isEqualTo(5000L);
            assertThat(p.autorizacion().cacheTtl()).isEqualTo(Duration.ofSeconds(60));
        });
    }

    @Test
    void sinExchangeNoArranca() {
        contexto.withPropertyValues("biohub.eventos.exchange=")
                .run(ctx -> assertThat(ctx).hasFailed()
                        .getFailure().rootCause().hasMessageContaining("eventos.exchange"));
    }

    @Test
    void unIntervaloNoNumericoNoArranca() {
        contexto.withPropertyValues("biohub.eventos.intervalo-ms=cinco")
                .run(ctx -> assertThat(ctx).hasFailed());
    }
}
