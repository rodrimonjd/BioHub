package co.edu.unbosque.biohub.autorizacion.visibilidad;

import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PoliticaVisibilidadUbicacionTest {

    private final PoliticaVisibilidadUbicacion politica = new PoliticaVisibilidadUbicacion();

    @Test
    void conPermisoSiempreVeCoordenadasExactas() {
        var permisos = Set.of("ubicacion:ver_exacta");
        assertEquals(NivelVisibilidad.EXACTA, politica.resolver(permisos, "CR"));
    }

    @Test
    void publicoSoloVeDepartamentoParaEspeciesCriticas() {
        assertEquals(NivelVisibilidad.DEPARTAMENTO, politica.resolver(Set.of("hallazgo:ver"), "CR"));
        assertEquals(NivelVisibilidad.DEPARTAMENTO, politica.resolver(Set.of("hallazgo:ver"), "en"));
    }

    @Test
    void publicoVeRegionParaVulnerablesYDesconocidas() {
        assertEquals(NivelVisibilidad.REGION, politica.resolver(Set.of(), "VU"));
        assertEquals(NivelVisibilidad.REGION, politica.resolver(Set.of(), null));
    }

    @Test
    void sinPermisoNuncaVeCoordenadasExactas() {
        for (String categoria : new String[]{"LC", "NT", "VU", "EN", "CR", "lc", "", "DD"}) {
            assertNotEquals(NivelVisibilidad.EXACTA, politica.resolver(Set.of("hallazgo:ver"), categoria));
        }
        assertEquals(NivelVisibilidad.REGION, politica.resolver(Set.of(), "LC"));
        assertEquals(NivelVisibilidad.REGION, politica.resolver(Set.of(), "NT"));
    }
}
