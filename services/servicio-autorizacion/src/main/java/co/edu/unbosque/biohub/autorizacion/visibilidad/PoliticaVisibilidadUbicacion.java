package co.edu.unbosque.biohub.autorizacion.visibilidad;

import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class PoliticaVisibilidadUbicacion {

    public static final String PERMISO_UBICACION_EXACTA = "ubicacion:ver_exacta";

    private static final Set<String> CRITICAS = Set.of("CR", "EN");
    private static final Set<String> VULNERABLES = Set.of("VU");
    private static final Set<String> BAJO_RIESGO = Set.of("LC", "NT");

    public NivelVisibilidad resolver(Set<String> permisos, String categoriaUicn) {
        if (permisos.contains(PERMISO_UBICACION_EXACTA)) {
            return NivelVisibilidad.EXACTA;
        }
        String c = categoriaUicn == null ? "" : categoriaUicn.trim().toUpperCase();
        if (CRITICAS.contains(c)) return NivelVisibilidad.DEPARTAMENTO;
        if (VULNERABLES.contains(c)) return NivelVisibilidad.REGION;
        if (BAJO_RIESGO.contains(c)) return NivelVisibilidad.EXACTA;
        return NivelVisibilidad.REGION;
    }
}
