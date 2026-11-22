package co.edu.unbosque.biohub.consulta.consulta;

import co.edu.unbosque.biohub.consulta.especie.EspecieInfo;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLectura;
import co.edu.unbosque.biohub.consulta.visibilidad.NivelVisibilidad;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PresentadorUbicacionTest {

    private static final EspecieInfo CAIMAN =
            new EspecieInfo("e1", "Crocodylus intermedius", "Caimán del Orinoco", "CR");

    private static HallazgoLectura hallazgo() {
        HallazgoLectura h = new HallazgoLectura("h1");
        h.registrar("e1", "Crocodylus intermedius", Instant.parse("2026-11-01T10:00:00Z"), "Adulto en la orilla",
                4.5, -71.3, "Meta", "Sierra de la Macarena", 1);
        return h;
    }

    @Test
    void exactaMuestraCoordenadasYRegion() {
        VistaHallazgoPublico v = PresentadorUbicacion.presentar(hallazgo(), CAIMAN, NivelVisibilidad.EXACTA);
        assertEquals(4.5, v.latitud());
        assertEquals(-71.3, v.longitud());
        assertEquals("Meta", v.departamento());
        assertEquals("Sierra de la Macarena", v.region());
        assertEquals("CR", v.categoriaConservacion());
        assertEquals("Caimán del Orinoco", v.nombreComun());
    }

    @Test
    void regionOcultaCoordenadas() {
        VistaHallazgoPublico v = PresentadorUbicacion.presentar(hallazgo(), CAIMAN, NivelVisibilidad.REGION);
        assertNull(v.latitud());
        assertNull(v.longitud());
        assertEquals("Meta", v.departamento());
        assertEquals("Sierra de la Macarena", v.region());
    }

    @Test
    void departamentoSoloMuestraElDepartamento() {
        VistaHallazgoPublico v = PresentadorUbicacion.presentar(hallazgo(), CAIMAN, NivelVisibilidad.DEPARTAMENTO);
        assertNull(v.latitud());
        assertNull(v.longitud());
        assertNull(v.region());
        assertEquals("Meta", v.departamento());
        assertEquals(NivelVisibilidad.DEPARTAMENTO, v.nivelUbicacion());
    }

    @Test
    void sinEspecieEnCatalogoNoFalla() {
        VistaHallazgoPublico v = PresentadorUbicacion.presentar(hallazgo(), null, NivelVisibilidad.REGION);
        assertNull(v.categoriaConservacion());
        assertEquals("Crocodylus intermedius", v.especie());
    }
}
