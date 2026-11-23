package co.edu.unbosque.biohub.historial.evento;

import co.edu.unbosque.biohub.historial.cambio.TipoCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public record CambioPropuesto(TipoRegistro tipoRegistro, String idRegistro, int version, TipoCambio tipoCambio,
                              String campo, String valorAnterior, String valorNuevo, String motivo,
                              String autor, String autorizador, Instant ocurrioEn,
                              Map<String, Object> estadoInicial,
                              UnaryOperator<Map<String, Object>> transformacion) {

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
