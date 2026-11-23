package co.edu.unbosque.biohub.historial.evento;

import co.edu.unbosque.biohub.historial.cambio.TipoCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Cambio ya traducido desde un evento, listo para guardarse.
 * Sabe calcular el estado del registro después del cambio a partir del estado anterior.
 */
public record CambioPropuesto(TipoRegistro tipoRegistro, String idRegistro, int version, TipoCambio tipoCambio,
                              String campo, String valorAnterior, String valorNuevo, String motivo,
                              String autor, String autorizador, Instant ocurrioEn,
                              Map<String, Object> estadoInicial,
                              UnaryOperator<Map<String, Object>> transformacion) {

    /**
     * Estado después del cambio. En la creación es el estado inicial; en los demás casos se aplica el cambio
     * sobre una copia del estado anterior (que nunca se modifica). Devuelve null si no se conoce el anterior.
     */
    public Map<String, Object> estadoResultante(Map<String, Object> estadoAnterior) {
        if (tipoCambio == TipoCambio.CREACION) {
            return new LinkedHashMap<>(estadoInicial);
        }
        if (estadoAnterior == null) {
            return null;
        }
        return transformacion.apply(new LinkedHashMap<>(estadoAnterior));
    }
}
