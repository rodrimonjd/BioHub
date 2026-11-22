package co.edu.unbosque.biohub.registro_service.expedicion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.registro_service.equipo.EquipoCampo;
import co.edu.unbosque.biohub.registro_service.equipo.EstadoEquipo;

class ExpedicionTest {

    private final LocalDate inicio = LocalDate.of(2026, 11, 1);

    @Test
    void exigeLider() {
        assertThatThrownBy(() -> new Expedicion("Orinoquia", "Meta", inicio, null, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("liderId");
    }

    @Test
    void rechazaFechaFinAnteriorAlInicio() {
        assertThatThrownBy(() -> new Expedicion("Orinoquia", "Meta", inicio, inicio.minusDays(1), "inv-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void asignarEquipoLoDejaAlCuidadoDelLider() {
        Expedicion expedicion = new Expedicion("Orinoquia", "Meta", inicio, null, "inv-1");
        EquipoCampo equipo = new EquipoCampo("CT-01", "camara trampa", null);

        expedicion.asignarEquipo(equipo);

        assertThat(expedicion.listarEquipos()).containsExactly(equipo);
        assertThat(equipo.getResponsableId()).isEqualTo("inv-1");
    }

    @Test
    void noAsignaEquiposEnMantenimiento() {
        Expedicion expedicion = new Expedicion("Orinoquia", "Meta", inicio, null, "inv-1");
        EquipoCampo equipo = new EquipoCampo("CT-02", "camara trampa", null);
        equipo.cambiarEstado(EstadoEquipo.MANTENIMIENTO);

        assertThatThrownBy(() -> expedicion.asignarEquipo(equipo))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(expedicion.listarEquipos()).isEmpty();
        assertThat(equipo.getResponsableId()).isNull();
    }
}
