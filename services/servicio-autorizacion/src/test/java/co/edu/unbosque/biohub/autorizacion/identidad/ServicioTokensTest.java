package co.edu.unbosque.biohub.autorizacion.identidad;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioTokensTest {

    @TempDir
    private Path carpeta;

    private PropiedadesIdentidad propiedades(String secreto, Duration duracion) {
        return new PropiedadesIdentidad(carpeta.resolve("claves/firma.json"), "biohub-autorizacion", duracion,
                Duration.ofMinutes(1), secreto);
    }

    private ServicioTokens servicio(PropiedadesIdentidad propiedades) throws Exception {
        return new ServicioTokens(ClaveFirma.cargarOCrear(propiedades.claveArchivo()), propiedades);
    }

    private JwtDecoder verificador(PropiedadesIdentidad propiedades) throws Exception {
        RSAKey clave = ClaveFirma.cargarOCrear(propiedades.claveArchivo());
        return new ConfiguracionIdentidad().verificadorDeTokens(clave, propiedades);
    }

    @Test
    void elTokenIdentificaAlUsuarioVenceYNoLlevaPermisos() throws Exception {
        PropiedadesIdentidad p = propiedades("s", Duration.ofHours(8));
        ServicioTokens.Token token = servicio(p).paraSesion("investigador.campo");

        Jwt leido = verificador(p).decode(token.valor());

        assertThat(leido.getSubject()).isEqualTo("investigador.campo");
        assertThat(leido.getExpiresAt()).isEqualTo(token.venceEn());
        assertThat(token.venceEn()).isBetween(Instant.now().plus(Duration.ofMinutes(479)), Instant.now().plus(Duration.ofMinutes(481)));
        assertThat(leido.getClaims()).containsOnlyKeys("sub", "iss", "aud", "iat", "exp", "jti");
    }

    @Test
    void unTokenVencidoNoSeAcepta() throws Exception {
        PropiedadesIdentidad p = propiedades("s", Duration.ofMinutes(-5));
        String token = servicio(p).paraSesion("admin").valor();

        assertThatThrownBy(() -> verificador(p).decode(token)).isInstanceOf(BadJwtException.class);
    }

    @Test
    void laClaveSeConservaAlReiniciar() throws Exception {
        PropiedadesIdentidad p = propiedades("s", Duration.ofHours(8));
        String antes = servicio(p).paraSesion("admin").valor();

        assertThat(verificador(p).decode(antes).getSubject()).isEqualTo("admin");
        assertThat(Files.exists(p.claveArchivo())).isTrue();
    }

    @Test
    void soloSePublicaLaClavePublica() throws Exception {
        List<JWK> claves = JWKSet.parse(servicio(propiedades("s", Duration.ofHours(8))).clavesPublicas()).getKeys();

        assertThat(claves).hasSize(1);
        assertThat(claves.get(0).isPrivate()).isFalse();
        assertThat(claves.get(0).toJSONObject()).containsOnlyKeys("kty", "kid", "n", "e");
    }

    @Test
    void elSecretoDeServiciosDebeCoincidirYEstarConfigurado() throws Exception {
        ServicioTokens conSecreto = servicio(propiedades("secreto", Duration.ofHours(8)));
        assertThat(conSecreto.esServicioConfiable("secreto")).isTrue();
        assertThat(conSecreto.esServicioConfiable("otro")).isFalse();
        assertThat(conSecreto.esServicioConfiable(null)).isFalse();

        ServicioTokens sinSecreto = servicio(propiedades("", Duration.ofHours(8)));
        assertThat(sinSecreto.esServicioConfiable("")).isFalse();
        assertThat(sinSecreto.esServicioConfiable(null)).isFalse();
    }
}
