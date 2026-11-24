package co.edu.unbosque.biohub.autorizacion.visibilidad;

import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class PoliticaVisibilidadUbicacion {

    public static final String PERMISO_UBICACION_EXACTA = "ubicacion:ver_exacta";

    private static final Set<String> CRITICAS = Set.of("CR", "EN");

    public NivelVisibilidad resolver(Set<String> permisos, String categoriaUicn) {
        if (permisos.contains(PERMISO_UBICACION_EXACTA)) {
            return NivelVisibilidad.EXACTA;
        }
        String c = categoriaUicn == null ? "" : categoriaUicn.trim().toUpperCase();
        return CRITICAS.contains(c) ? NivelVisibilidad.DEPARTAMENTO : NivelVisibilidad.REGION;
    }
}
