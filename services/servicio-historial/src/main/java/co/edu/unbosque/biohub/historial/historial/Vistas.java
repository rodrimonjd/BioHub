package co.edu.unbosque.biohub.historial.historial;

import co.edu.unbosque.biohub.historial.cambio.RegistroCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class Vistas {

    private Vistas() {}

    public record VistaCambio(int version, TipoCambio tipoCambio, String campo, String valorAnterior,
                              String valorNuevo, String motivo, String autor, String autorizador,
                              Instant ocurrioEn, Instant registradoEn, String descripcion) {
        public static VistaCambio desde(RegistroCambio c) {
            return new VistaCambio(c.getVersion(), c.getTipoCambio(), c.getCampo(), c.getValorAnterior(),
                    c.getValorNuevo(), c.getMotivo(), c.getAutor(), c.getAutorizador(), c.getOcurrioEn(),
                    c.getRegistradoEn(), c.describir());
        }
    }

    public record VistaHistorial(TipoRegistro tipoRegistro, String idRegistro, int versionActual,
                                 int totalModificaciones, boolean completo, List<Integer> versionesFaltantes,
                                 String mensaje, List<VistaCambio> cambios) {}

    public record ResumenVersion(int version, Instant creadaEn, String autor, TipoCambio tipoCambio,
                                 String descripcion) {}

    public record VistaVersiones(TipoRegistro tipoRegistro, String idRegistro, int versionActual,
                                 String mensaje, List<ResumenVersion> versiones) {}

    public record VistaVersion(TipoRegistro tipoRegistro, String idRegistro, int version, boolean esActual,
                               Instant creadaEn, String autor, Map<String, Object> estado, String mensaje) {}

    public record VistaIntegridad(TipoRegistro tipoRegistro, String idRegistro, boolean integro,
                                  int cambiosVerificados, List<String> problemas) {}
}
