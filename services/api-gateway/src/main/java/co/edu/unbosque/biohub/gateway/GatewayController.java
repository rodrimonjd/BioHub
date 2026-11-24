package co.edu.unbosque.biohub.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class GatewayController {

    public static final String PERMISO_MONITOREO = "api_externa:monitorear";
    public static final String PERMISO_CONFIGURAR = "api_externa:configurar";

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    private final ApiGateway gateway;
    private final ClienteAutorizacion autorizacion;

    public GatewayController(ApiGateway gateway, ClienteAutorizacion autorizacion) {
        this.gateway = gateway;
        this.autorizacion = autorizacion;
    }

    public record ResumenConsumo(String urlPublica, int limiteMaximo, List<ApiGateway.ConsumoConsumidor> consumidores) { }

    public record SolicitudLimite(Integer limitePorMinuto) { }

    @GetMapping("/gateway/consumo")
    public ResumenConsumo consumo(@RequestHeader(value = ApiGateway.CABECERA_USUARIO, required = false) String usuarioId) {
        exigir(usuarioId, PERMISO_MONITOREO);
        return resumen();
    }

    @PutMapping("/gateway/limite")
    public ResumenConsumo cambiarLimite(@RequestHeader(value = ApiGateway.CABECERA_USUARIO, required = false) String usuarioId,
                                        @RequestBody SolicitudLimite solicitud) {
        exigir(usuarioId, PERMISO_CONFIGURAR);
        if (solicitud.limitePorMinuto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El limite por minuto es obligatorio");
        }
        try {
            gateway.cambiarLimite(solicitud.limitePorMinuto());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        log.info("Limite por minuto cambiado a {} por {}", solicitud.limitePorMinuto(), usuarioId.trim());
        return resumen();
    }

    private ResumenConsumo resumen() {
        return new ResumenConsumo(gateway.getUrlPublica(), gateway.getLimiteMaximo(), gateway.consumo());
    }

    @GetMapping("/api/v1/**")
    public ResponseEntity<String> consultar(HttpServletRequest solicitud, @RequestHeader(value = ApiGateway.CABECERA_API_KEY, required = false) String apiKey) {
        return gateway.enrutar(solicitud.getRequestURI(), solicitud.getQueryString(), apiKey);
    }

    private void exigir(String usuarioId, String permiso) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Se requiere iniciar sesion con el permiso " + permiso);
        }
        boolean permitido;
        try {
            permitido = autorizacion.tienePermiso(usuarioId.trim(), permiso);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de autorizacion no responde");
        }
        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el permiso " + permiso);
        }
    }
}
