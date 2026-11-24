package co.edu.unbosque.biohub.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class GatewayController {

    /** Permiso para ver el consumo de la API externa; el público general (CIUDADANO) no lo tiene. */
    public static final String PERMISO_MONITOREO = "api_externa:monitorear";

    private final ApiGateway gateway;
    private final ClienteAutorizacion autorizacion;

    public GatewayController(ApiGateway gateway, ClienteAutorizacion autorizacion) {
        this.gateway = gateway;
        this.autorizacion = autorizacion;
    }

    public record ResumenConsumo(String urlPublica, List<ApiGateway.ConsumoConsumidor> consumidores) { }

    @GetMapping("/gateway/consumo")
    public ResumenConsumo consumo(@RequestHeader(value = ApiGateway.CABECERA_USUARIO, required = false) String usuarioId) {
        exigirMonitoreo(usuarioId);
        return new ResumenConsumo(gateway.getUrlPublica(), gateway.consumo());
    }

    @GetMapping("/api/v1/**")
    public ResponseEntity<String> consultar(HttpServletRequest solicitud, @RequestHeader(value = ApiGateway.CABECERA_API_KEY, required = false) String apiKey) {
        return gateway.enrutar(solicitud.getRequestURI(), solicitud.getQueryString(), apiKey);
    }

    private void exigirMonitoreo(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Se requiere la cabecera " + ApiGateway.CABECERA_USUARIO + " con el permiso " + PERMISO_MONITOREO);
        }
        boolean permitido;
        try {
            permitido = autorizacion.tienePermiso(usuarioId.trim(), PERMISO_MONITOREO);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de autorizacion no responde");
        }
        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el permiso " + PERMISO_MONITOREO);
        }
    }
}
