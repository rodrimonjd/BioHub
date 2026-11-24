package co.edu.unbosque.biohub.registro_service.configuracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/** La configuración se enlaza con tipos y se valida al arrancar: un valor faltante o inválido impide iniciar. */
class PropiedadesRegistroTest {

    @Configuration
    @EnableConfigurationProperties(PropiedadesRegistro.class)
    static class Config {
    }

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(Config.class);

    private static final String[] VALIDAS = {
            "biohub.eventos.exchange=biohub.eventos",
            "biohub.eventos.intervalo-ms=5000",
            "biohub.eventos.lote=50",
            "biohub.autorizacion.url=http://localhost:8082",
            "biohub.autorizacion.cache-ttl=60s"
    };

    @Test
    void enlazaLosValoresConSuTipo() {
        contexto.withPropertyValues(VALIDAS).run(ctx -> {
            PropiedadesRegistro p = ctx.getBean(PropiedadesRegistro.class);
            assertThat(p.eventos().lote()).isEqualTo(50);
            assertThat(p.autorizacion().url().getPort()).isEqualTo(8082);
            assertThat(p.autorizacion().cacheTtl()).isEqualTo(Duration.ofSeconds(60));
        });
    }

    @Test
    void sinUrlDeAutorizacionNoArranca() {
        contexto.withPropertyValues(
                        "biohub.eventos.exchange=biohub.eventos",
                        "biohub.eventos.intervalo-ms=5000",
                        "biohub.eventos.lote=50",
                        "biohub.autorizacion.cache-ttl=60s")
                .run(ctx -> assertThat(ctx).hasFailed()
                        .getFailure().rootCause().hasMessageContaining("autorizacion.url"));
    }

    @Test
    void unLoteNegativoNoArranca() {
        contexto.withPropertyValues(VALIDAS).withPropertyValues("biohub.eventos.lote=-1")
                .run(ctx -> assertThat(ctx).hasFailed());
    }

    @Test
    void unaDuracionMalEscritaNoArranca() {
        contexto.withPropertyValues(VALIDAS).withPropertyValues("biohub.autorizacion.cache-ttl=60x")
                .run(ctx -> assertThat(ctx).hasFailed());
    }
}
