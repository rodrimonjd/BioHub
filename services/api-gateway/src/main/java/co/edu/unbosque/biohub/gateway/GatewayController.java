package co.edu.unbosque.biohub.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class GatewayController {

    private final ApiGateway gateway;

    public GatewayController(ApiGateway gateway) {
        this.gateway = gateway;
    }

    public record ResumenConsumo(String urlPublica, List<ApiGateway.ConsumoConsumidor> consumidores) { }

    @GetMapping("/gateway/consumo")
    public ResumenConsumo consumo() {
        return new ResumenConsumo(gateway.getUrlPublica(), gateway.consumo());
    }

    @GetMapping("/api/v1/**")
    public ResponseEntity<String> consultar(HttpServletRequest solicitud, @RequestHeader(value = ApiGateway.CABECERA_API_KEY, required = false) String apiKey) {
        return gateway.enrutar(solicitud.getRequestURI(), solicitud.getQueryString(), apiKey);
    }
}
