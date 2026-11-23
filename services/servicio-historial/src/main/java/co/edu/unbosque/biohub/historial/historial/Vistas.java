package co.edu.unbosque.biohub.historial.historial;

import co.edu.unbosque.biohub.historial.cambio.RegistroCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoCambio;
import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Respuestas de la API del historial. */
public final class Vistas {

    private Vistas() {}

    /** Un cambio con su responsable (HU-09, HU-10). */
    public record VistaCambio(int version, TipoCambio tipoCambio, String campo, String valorAnterior,
                              String valorNuevo, String motivo, String autor, String autorizador,
                              Instant ocurrioEn, Instant registradoEn, String descripcion) {
        public static VistaCambio desde(RegistroCambio c) {
            return new VistaCambio(c.getVersion(), c.getTipoCambio(), c.getCampo(), c.getValorAnterior(),
                    c.getValorNuevo(), c.getMotivo(), c.getAutor(), c.getAutorizador(), c.getOcurrioEn(),
                    c.getRegistradoEn(), c.describir());
        }
    }

    /** Historial cronológico completo de un registro (HU-09, HU-19.1, HU-19.2). */
    public record VistaHistorial(TipoRegistro tipoRegistro, String idRegistro, int versionActual,
                                 int totalModificaciones, boolean completo, List<Integer> versionesFaltantes,
                                 String mensaje, List<VistaCambio> cambios) {}

    /** Resumen de una versión para la lista de versiones (HU-20.2, HU-20.3). */
    public record ResumenVersion(int version, Instant creadaEn, String autor, TipoCambio tipoCambio,
                                 String descripcion) {}

    public record VistaVersiones(TipoRegistro tipoRegistro, String idRegistro, int versionActual,
                                 String mensaje, List<ResumenVersion> versiones) {}

    /** Estado completo del registro en una versión: "qué se sabía en esa fecha" (HU-20.1, HU-20.2). */
    public record VistaVersion(TipoRegistro tipoRegistro, String idRegistro, int version, boolean esActual,
                               Instant creadaEn, String autor, Map<String, Object> estado, String mensaje) {}

    /** Resultado de verificar que nada fue alterado ni borrado (HU-19.3). */
    public record VistaIntegridad(TipoRegistro tipoRegistro, String idRegistro, boolean integro,
                                  int cambiosVerificados, List<String> problemas) {}
}
