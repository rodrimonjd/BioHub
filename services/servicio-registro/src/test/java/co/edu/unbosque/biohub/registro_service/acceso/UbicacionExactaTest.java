package co.edu.unbosque.biohub.registro_service.acceso;

import co.edu.unbosque.biohub.registro_service.avistamiento.Avistamiento;
import co.edu.unbosque.biohub.registro_service.avistamiento.AvistamientoControlador;
import co.edu.unbosque.biohub.registro_service.avistamiento.AvistamientoServicio;
import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.hallazgo.ConstructorHallazgo;
import co.edu.unbosque.biohub.registro_service.hallazgo.HallazgoControlador;
import co.edu.unbosque.biohub.registro_service.hallazgo.HallazgoServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AvistamientoControlador.class, HallazgoControlador.class})
@Import(VerificadorPermisos.class)
class UbicacionExactaTest {

    private static final Especie CAIMAN = new Especie("Crocodylus intermedius", "Caimán del Orinoco", "CR");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AvistamientoServicio avistamientos;

    @MockitoBean
    private HallazgoServicio hallazgos;

    @MockitoBean
    private ClienteAutorizacion autorizacion;

    private void hayDatosConCoordenadas() {
        when(avistamientos.listar()).thenReturn(List.of(
                new Avistamiento("investigador.campo", CAIMAN, Instant.parse("2026-11-01T10:00:00Z"), 3,
                        5.5412, -68.5194, "Vichada", "Orinoquía")));
        when(hallazgos.listar(null)).thenReturn(List.of(new ConstructorHallazgo()
                .investigador("investigador.campo")
                .especie(CAIMAN)
                .fecha(Instant.parse("2026-11-01T10:00:00Z"))
                .descripcion("Hembra adulta en banco de arena")
                .ubicacion(5.5412, -68.5194, "Vichada", "Orinoquía")
                .build()));
    }

    @Test
    void sinSesionNoSeEnvianCoordenadas() throws Exception {
        hayDatosConCoordenadas();

        mvc.perform(get("/avistamientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].latitud").doesNotExist())
                .andExpect(jsonPath("$[0].longitud").doesNotExist())
                .andExpect(jsonPath("$[0].departamento").value("Vichada"));
        mvc.perform(get("/hallazgos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].latitud").doesNotExist())
                .andExpect(jsonPath("$[0].region").value("Orinoquía"));
    }

    @Test
    void sinPermisoUbicacionExactaNoSeEnvianCoordenadas() throws Exception {
        hayDatosConCoordenadas();
        when(autorizacion.tienePermiso("investigador.campo", "ubicacion:ver_exacta")).thenReturn(false);

        mvc.perform(get("/avistamientos").header("X-Usuario-Id", "investigador.campo"))
                .andExpect(jsonPath("$[0].latitud").doesNotExist());
        mvc.perform(get("/hallazgos").header("X-Usuario-Id", "investigador.campo"))
                .andExpect(jsonPath("$[0].latitud").doesNotExist());
    }

    @Test
    void conAccesoALaEspecieSeEnvianSusCoordenadasAunqueNoTengaElPermisoGeneral() throws Exception {
        hayDatosConCoordenadas();
        when(autorizacion.tienePermiso("investigador.campo", "ubicacion:ver_exacta")).thenReturn(false);
        when(autorizacion.especiesConUbicacionExacta("investigador.campo"))
                .thenReturn(Set.of(CAIMAN.getId().toString()));

        mvc.perform(get("/avistamientos").header("X-Usuario-Id", "investigador.campo"))
                .andExpect(jsonPath("$[0].latitud").value(5.5412));
        mvc.perform(get("/hallazgos").header("X-Usuario-Id", "investigador.campo"))
                .andExpect(jsonPath("$[0].latitud").value(5.5412));
    }

    @Test
    void conAccesoAOtraEspecieNoSeEnvianLasCoordenadas() throws Exception {
        hayDatosConCoordenadas();
        when(autorizacion.tienePermiso("investigador.campo", "ubicacion:ver_exacta")).thenReturn(false);
        when(autorizacion.especiesConUbicacionExacta("investigador.campo"))
                .thenReturn(Set.of("22222222-2222-2222-2222-222222222222"));

        mvc.perform(get("/hallazgos").header("X-Usuario-Id", "investigador.campo"))
                .andExpect(jsonPath("$[0].latitud").doesNotExist());
    }

    @Test
    void conPermisoUbicacionExactaSeEnvianCoordenadas() throws Exception {
        hayDatosConCoordenadas();
        when(autorizacion.tienePermiso("autoridad.ambiental", "ubicacion:ver_exacta")).thenReturn(true);

        mvc.perform(get("/avistamientos").header("X-Usuario-Id", "autoridad.ambiental"))
                .andExpect(jsonPath("$[0].latitud").value(5.5412))
                .andExpect(jsonPath("$[0].longitud").value(-68.5194));
        mvc.perform(get("/hallazgos").header("X-Usuario-Id", "autoridad.ambiental"))
                .andExpect(jsonPath("$[0].latitud").value(5.5412));
    }
}
