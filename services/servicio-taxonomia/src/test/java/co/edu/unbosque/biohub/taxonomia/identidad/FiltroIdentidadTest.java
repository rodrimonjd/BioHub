package co.edu.unbosque.biohub.taxonomia.identidad;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class FiltroIdentidadTest {

    private static final String EMISOR = "biohub-autorizacion";
    private static final String CABECERA = "X-Usuario-Id";

    private RSAKey clave;
    private FiltroIdentidad filtro;

    @BeforeEach
    void preparar() throws Exception {
        clave = new RSAKeyGenerator(2048).keyID("k1").generate();
        NimbusJwtDecoder tokens = NimbusJwtDecoder.withPublicKey(clave.toRSAPublicKey()).build();
        tokens.setJwtValidator(ConfiguracionIdentidad.validador(EMISOR));
        filtro = new FiltroIdentidad(tokens);
    }

    private static JWTClaimsSet.Builder datos(String usuario) {
        return new JWTClaimsSet.Builder().subject(usuario).issuer(EMISOR).audience("biohub")
                .expirationTime(Date.from(Instant.now().plusSeconds(600)));
    }

    private static String firmar(JWTClaimsSet datos, RSAKey con) throws JOSEException {
        SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("k1").build(), datos);
        token.sign(new RSASSASigner(con));
        return token.serialize();
    }

    private record Resultado(int estado, HttpServletRequest solicitud, String cuerpo) {
        String usuario() { return solicitud.getHeader(CABECERA); }
    }

    private Resultado enviar(String credencial, String usuarioEscrito) throws Exception {
        return enviar(filtro, credencial, usuarioEscrito);
    }

    private static Resultado enviar(FiltroIdentidad filtro, String credencial, String usuarioEscrito) throws Exception {
        MockHttpServletRequest solicitud = new MockHttpServletRequest("GET", "/recurso");
        if (credencial != null) solicitud.addHeader("Authorization", credencial);
        if (usuarioEscrito != null) solicitud.addHeader("x-usuario-id", usuarioEscrito);
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(solicitud, respuesta, cadena);
        return new Resultado(respuesta.getStatus(), (HttpServletRequest) cadena.getRequest(), respuesta.getContentAsString());
    }

    @Test
    void laIdentidadSaleDelTokenYNoDeLaCabecera() throws Exception {
        Resultado r = enviar("Bearer " + firmar(datos("investigador.campo").build(), clave), "admin");

        assertThat(r.usuario()).isEqualTo("investigador.campo");
        assertThat(Collections.list(r.solicitud().getHeaders(CABECERA))).containsExactly("investigador.campo");
        assertThat(Collections.list(r.solicitud().getHeaderNames())).filteredOn(CABECERA::equalsIgnoreCase).hasSize(1);
    }

    @Test
    void sinTokenLaCabeceraEscritaPorElClienteSeBorra() throws Exception {
        Resultado r = enviar(null, "admin");

        assertThat(r.estado()).isEqualTo(200);
        assertThat(r.usuario()).isNull();
        assertThat(Collections.list(r.solicitud().getHeaders("X-USUARIO-ID"))).isEmpty();
        assertThat(Collections.list(r.solicitud().getHeaderNames())).noneMatch(CABECERA::equalsIgnoreCase);
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() throws Exception {
        rechazado("Bearer " + firmar(datos("admin").build(), new RSAKeyGenerator(2048).keyID("k1").generate()));
    }

    @Test
    void unTokenVencidoSeRechaza() throws Exception {
        rechazado("Bearer " + firmar(datos("admin").expirationTime(Date.from(Instant.now().minusSeconds(120))).build(), clave));
    }

    @Test
    void unTokenSinVencimientoSeRechaza() throws Exception {
        rechazado("Bearer " + firmar(datos("admin").expirationTime(null).build(), clave));
    }

    @Test
    void unTokenAlteradoSeRechaza() throws Exception {
        String[] partes = firmar(datos("investigador.campo").build(), clave).split("\\.");
        String otros = firmar(datos("admin").build(), new RSAKeyGenerator(2048).keyID("k1").generate()).split("\\.")[1];
        rechazado("Bearer " + partes[0] + "." + otros + "." + partes[2]);
    }

    @Test
    void unTokenSinFirmaSeRechaza() throws Exception {
        rechazado("Bearer " + new PlainJWT(datos("admin").build()).serialize());
    }

    @Test
    void unTokenFirmadoConLaClavePublicaComoSecretoSeRechaza() throws Exception {
        SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).keyID("k1").build(), datos("admin").build());
        token.sign(new MACSigner(clave.toRSAPublicKey().getEncoded()));
        rechazado("Bearer " + token.serialize());
    }

    @Test
    void unTokenDeOtroEmisorODestinoSeRechaza() throws Exception {
        rechazado("Bearer " + firmar(datos("admin").issuer("otro").build(), clave));
        rechazado("Bearer " + firmar(datos("admin").audience("otro").build(), clave));
    }

    @Test
    void unTokenSinUsuarioOUnaCredencialQueNoEsTokenSeRechaza() throws Exception {
        rechazado("Bearer " + firmar(datos(null).build(), clave));
        rechazado("Bearer basura");
        rechazado("Basic YWRtaW46YWRtaW4=");
        rechazado("");
    }

    @Test
    void siNoSePuedeVerificarNoSeDejaPasar() throws Exception {
        JwtDecoder caido = token -> { throw new JwtException("autorizacion caido"); };
        Resultado r = enviar(new FiltroIdentidad(caido), "Bearer x", null);

        assertThat(r.estado()).isEqualTo(503);
        assertThat(r.solicitud()).isNull();
    }

    private void rechazado(String credencial) throws Exception {
        Resultado r = enviar(credencial, "admin");
        assertThat(r.estado()).isEqualTo(401);
        assertThat(r.solicitud()).isNull();
        assertThat(r.cuerpo()).contains("\"estado\":401", "La sesión no es válida");
    }
}
