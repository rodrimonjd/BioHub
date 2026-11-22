package co.edu.unbosque.biohub.registro_service.avistamiento;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.registro_service.especie.Especie;

class AvistamientoTest {

    private final Especie especie = new Especie("Ara macao", "Guacamaya roja", "LC");
    private final Instant ayer = Instant.now().minus(1, ChronoUnit.DAYS);

    @Test
    void avistamientoCompletoEsValido() {
        Avistamiento a = new Avistamiento("inv-1", especie, ayer, 3, 4.5, -74.1, "Meta", null);
        assertThat(a.validarDatos()).isTrue();
    }

    @Test
    void avistamientoSinCoordenadasEsValido() {
        Avistamiento a = new Avistamiento("inv-1", especie, ayer, 1, null, null, "Meta", null);
        assertThat(a.validarDatos()).isTrue();
    }

    @Test
    void rechazaCantidadCero() {
        assertThat(new Avistamiento("inv-1", especie, ayer, 0, null, null, "Meta", null).validarDatos()).isFalse();
    }

    @Test
    void rechazaFechaFutura() {
        Instant manana = Instant.now().plus(1, ChronoUnit.DAYS);
        assertThat(new Avistamiento("inv-1", especie, manana, 1, null, null, "Meta", null).validarDatos()).isFalse();
    }

    @Test
    void rechazaLatitudSinLongitud() {
        assertThat(new Avistamiento("inv-1", especie, ayer, 1, 4.5, null, "Meta", null).validarDatos()).isFalse();
    }

    @Test
    void rechazaCoordenadasFueraDeRango() {
        assertThat(new Avistamiento("inv-1", especie, ayer, 1, 95.0, -74.0, "Meta", null).validarDatos()).isFalse();
    }

    @Test
    void rechazaSinInvestigadorNiDepartamento() {
        assertThat(new Avistamiento(" ", especie, ayer, 1, null, null, "Meta", null).validarDatos()).isFalse();
        assertThat(new Avistamiento("inv-1", especie, ayer, 1, null, null, "", null).validarDatos()).isFalse();
    }
}
