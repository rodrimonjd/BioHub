package co.edu.unbosque.biohub.autorizacion.identidad;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(PropiedadesIdentidad.class)
public class ConfiguracionIdentidad {

    static final String DESTINO = "biohub";

    @Bean
    public RSAKey claveDeFirma(PropiedadesIdentidad propiedades) {
        return ClaveFirma.cargarOCrear(propiedades.claveArchivo());
    }

    @Bean
    public JwtDecoder verificadorDeTokens(RSAKey clave, PropiedadesIdentidad propiedades) throws JOSEException {
        NimbusJwtDecoder tokens = NimbusJwtDecoder.withPublicKey(clave.toRSAPublicKey()).build();
        tokens.setJwtValidator(validador(propiedades.emisor()));
        return tokens;
    }

    @Bean
    public FilterRegistrationBean<FiltroIdentidad> filtroIdentidad(JwtDecoder tokens) {
        FilterRegistrationBean<FiltroIdentidad> registro = new FilterRegistrationBean<>(new FiltroIdentidad(tokens));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registro;
    }

    static OAuth2TokenValidator<Jwt> validador(String emisor) {
        return new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ofSeconds(30)),
                new JwtIssuerValidator(emisor),
                new JwtClaimValidator<>(JwtClaimNames.AUD, aud -> aud instanceof List<?> destinos && destinos.contains(DESTINO)),
                new JwtClaimValidator<>(JwtClaimNames.EXP, Objects::nonNull));
    }
}
