package co.edu.unbosque.biohub.registro_service.hallazgo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.evidencia.DatosInvalidosException;
import co.edu.unbosque.biohub.registro_service.evidencia.EvidenciaFactory;
import co.edu.unbosque.biohub.registro_service.evidencia.TipoEvidencia;
import co.edu.unbosque.biohub.registro_service.expedicion.Expedicion;

class HallazgoBuilderTest {

    private final Especie especie = new Especie("Atelopus varius", "Rana arlequin", "CR");

    private HallazgoBuilder builderValido() {
        return new HallazgoBuilder()
                .investigador("inv-1")
                .especie(especie)
                .fecha(Instant.now().minus(1, ChronoUnit.DAYS))
                .descripcion("Ejemplar adulto junto a la quebrada")
                .ubicacion(4.5, -74.1, "Cundinamarca", "Sabana");
    }

    @Test
    void construyeHallazgoValidoConVersionUnoYSinExpedicion() {
        Hallazgo h = builderValido().build();

        assertThat(h.getId()).isNotNull();
        assertThat(h.getVersionActual()).isEqualTo(1);
        assertThat(h.getInvestigadorId()).isEqualTo("inv-1");
        assertThat(h.getExpedicion()).isNull();
    }

    @Test
    void asignaLaExpedicionCuandoSeIndica() {
        Expedicion expedicion = new Expedicion("Orinoquia", "Meta",
                LocalDate.of(2026, 11, 1), null, "inv-1");

        Hallazgo h = builderValido().expedicion(expedicion).build();

        assertThat(h.getExpedicion()).isSameAs(expedicion);
    }

    @Test
    void agregaLasEvidenciasAlHallazgo() {
        Hallazgo h = builderValido()
                .agregarEvidencia(EvidenciaFactory.crear(TipoEvidencia.ADN, "M-001", null, null, null))
                .build();

        assertThat(h.getEvidencias()).hasSize(1);
        assertThat(h.getEvidencias().get(0).getHallazgo()).isSameAs(h);
    }

    @Test
    void rechazaHallazgoSinEspecie() {
        assertThatThrownBy(() -> builderValido().especie(null).build())
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("especie");
    }

    @Test
    void rechazaFechaFutura() {
        assertThatThrownBy(() -> builderValido().fecha(Instant.now().plus(1, ChronoUnit.DAYS)).build())
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("futuro");
    }

    @Test
    void rechazaLatitudSinLongitud() {
        assertThatThrownBy(() -> builderValido().ubicacion(4.5, null, "Meta", null).build())
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("juntas");
    }

    @Test
    void rechazaInvestigadorVacio() {
        assertThatThrownBy(() -> builderValido().investigador(" ").build())
                .isInstanceOf(DatosInvalidosException.class);
    }
}
