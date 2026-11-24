package co.edu.unbosque.biohub.autorizacion.identidad;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class ServicioTokens {

    public record Token(String valor, Instant venceEn) {}

    private final RSAKey clave;
    private final JWSSigner firmante;
    private final PropiedadesIdentidad propiedades;

    public ServicioTokens(RSAKey clave, PropiedadesIdentidad propiedades) throws JOSEException {
        this.clave = clave;
        this.firmante = new RSASSASigner(clave);
        this.propiedades = propiedades;
    }

    public Token paraSesion(String usuarioId) {
        return emitir(usuarioId, propiedades.duracionSesion());
    }

    public Token paraConsumidor(String consumidorId) {
        return emitir(consumidorId, propiedades.duracionConsumidor());
    }

    public boolean esServicioConfiable(String secreto) {
        String esperado = propiedades.secretoServicios();
        return esperado != null && !esperado.isBlank() && secreto != null
                && MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8), secreto.getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, Object> clavesPublicas() {
        return new JWKSet(clave.toPublicJWK()).toJSONObject();
    }

    private Token emitir(String usuarioId, Duration duracion) {
        Instant ahora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant vence = ahora.plus(duracion);
        JWTClaimsSet datos = new JWTClaimsSet.Builder()
                .subject(usuarioId)
                .issuer(propiedades.emisor())
                .audience(ConfiguracionIdentidad.DESTINO)
                .issueTime(Date.from(ahora))
                .expirationTime(Date.from(vence))
                .jwtID(UUID.randomUUID().toString())
                .build();
        SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(clave.getKeyID()).build(), datos);
        try {
            token.sign(firmante);
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo firmar el token", e);
        }
        return new Token(token.serialize(), vence);
    }
}
