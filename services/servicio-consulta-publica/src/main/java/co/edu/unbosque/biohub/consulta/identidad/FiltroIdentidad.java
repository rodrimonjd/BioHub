package co.edu.unbosque.biohub.consulta.identidad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class FiltroIdentidad extends OncePerRequestFilter {

    public static final String CABECERA_USUARIO = "X-Usuario-Id";

    private static final Logger log = LoggerFactory.getLogger(FiltroIdentidad.class);
    private static final String PREFIJO = "Bearer ";

    private final JwtDecoder tokens;

    public FiltroIdentidad(JwtDecoder tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest solicitud, @NonNull HttpServletResponse respuesta,
                                    @NonNull FilterChain cadena) throws ServletException, IOException {
        String credencial = solicitud.getHeader(HttpHeaders.AUTHORIZATION);
        if (credencial == null) {
            cadena.doFilter(new SolicitudConIdentidad(solicitud, null), respuesta);
            return;
        }
        String usuario;
        try {
            usuario = usuarioDe(credencial);
        } catch (BadJwtException e) {
            log.warn("Token rechazado en {}: {}", solicitud.getRequestURI(), e.getMessage());
            rechazar(respuesta, HttpStatus.UNAUTHORIZED, "La sesión no es válida o ya venció");
            return;
        } catch (JwtException e) {
            log.warn("No se pudo verificar el token en {}: {}", solicitud.getRequestURI(), e.getMessage());
            rechazar(respuesta, HttpStatus.SERVICE_UNAVAILABLE, "No se pudo verificar la sesión: servicio de autorización no disponible");
            return;
        }
        cadena.doFilter(new SolicitudConIdentidad(solicitud, usuario), respuesta);
    }

    private String usuarioDe(String credencial) {
        if (!credencial.regionMatches(true, 0, PREFIJO, 0, PREFIJO.length())) {
            throw new BadJwtException("Se esperaba un token Bearer");
        }
        String usuario = tokens.decode(credencial.substring(PREFIJO.length()).trim()).getSubject();
        if (usuario == null || usuario.isBlank()) {
            throw new BadJwtException("El token no identifica a ningún usuario");
        }
        return usuario;
    }

    private static void rechazar(HttpServletResponse respuesta, HttpStatus estado, String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        if (estado == HttpStatus.UNAUTHORIZED) {
            respuesta.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\"");
        }
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write("{\"estado\":" + estado.value() + ",\"error\":\"" + estado.getReasonPhrase()
                + "\",\"mensaje\":\"" + mensaje + "\"}");
    }

    private static final class SolicitudConIdentidad extends HttpServletRequestWrapper {

        private final String usuario;

        private SolicitudConIdentidad(HttpServletRequest solicitud, String usuario) {
            super(solicitud);
            this.usuario = usuario;
        }

        @Override
        public String getHeader(String nombre) {
            return CABECERA_USUARIO.equalsIgnoreCase(nombre) ? usuario : super.getHeader(nombre);
        }

        @Override
        public Enumeration<String> getHeaders(String nombre) {
            if (!CABECERA_USUARIO.equalsIgnoreCase(nombre)) return super.getHeaders(nombre);
            return usuario == null ? Collections.emptyEnumeration() : Collections.enumeration(List.of(usuario));
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> nombres = new ArrayList<>(Collections.list(super.getHeaderNames()));
            nombres.removeIf(CABECERA_USUARIO::equalsIgnoreCase);
            if (usuario != null) nombres.add(CABECERA_USUARIO);
            return Collections.enumeration(nombres);
        }
    }
}
