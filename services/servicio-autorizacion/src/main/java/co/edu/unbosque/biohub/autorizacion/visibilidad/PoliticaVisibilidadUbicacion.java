package co.edu.unbosque.biohub.autorizacion.visibilidad;

import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * Decide si se muestran coordenadas exactas o solo región/departamento.
 * Es el punto de extensión (patrón Strategy) para las reglas de ASR-04 y ASR-05.
 *
 * Reglas actuales (ajustables a lo que digan los ASR):
 *  1. Quien tenga el permiso 'ubicacion:ver_exacta' siempre ve coordenadas exactas.
 *  2. Para el resto, depende de la categoría UICN de la especie:
 *       CR, EN       -> solo DEPARTAMENTO
 *       VU           -> solo REGION
 *       LC, NT       -> EXACTA
 *       desconocida  -> REGION (criterio conservador)
 */
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
