package co.edu.unbosque.biohub.registro_service.hallazgo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.evidencia.DatosInvalidosException;

class HallazgoTest {

    private final Especie especie = new Especie("Atelopus varius", "Rana arlequin", "CR");

    private Hallazgo nuevoHallazgo() {
        return new HallazgoBuilder()
                .investigador("inv-1")
                .especie(especie)
                .fecha(Instant.now())
                .descripcion("Descripcion inicial")
                .ubicacion(null, null, "Meta", null)
                .build();
    }

    @Test
    void modificarCambiaElCampoSubeLaVersionYDevuelveElCambio() {
        Hallazgo h = nuevoHallazgo();

        CambioHallazgo cambio = h.modificar("descripcion", "Descripcion corregida", "inv-1");

        assertThat(h.getDescripcion()).isEqualTo("Descripcion corregida");
        assertThat(h.getVersionActual()).isEqualTo(2);
        assertThat(cambio.valorAnterior()).isEqualTo("Descripcion inicial");
        assertThat(cambio.valorNuevo()).isEqualTo("Descripcion corregida");
        assertThat(cambio.autor()).isEqualTo("inv-1");
    }

    @Test
    void modificarRechazaCamposNoPermitidos() {
        Hallazgo h = nuevoHallazgo();

        assertThatThrownBy(() -> h.modificar("latitud", "4.5", "inv-1"))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("no modificable");
    }

    @Test
    void modificarRechazaElMismoValor() {
        Hallazgo h = nuevoHallazgo();

        assertThatThrownBy(() -> h.modificar("departamento", "Meta", "inv-1"))
                .isInstanceOf(DatosInvalidosException.class);
        assertThat(h.getVersionActual()).isEqualTo(1);
    }

    @Test
    void reclasificarGuardaAutorizadorYMotivo() {
        Hallazgo h = nuevoHallazgo();
        Especie nueva = new Especie("Atelopus zeteki", "Rana dorada", "CR");

        CambioHallazgo cambio = h.reclasificar(nueva, "Revision de ADN", "inv-1", "aut-9");

        assertThat(h.getEspecie()).isSameAs(nueva);
        assertThat(h.getVersionActual()).isEqualTo(2);
        assertThat(cambio.valorAnterior()).isEqualTo("Atelopus varius");
        assertThat(cambio.autorizador()).isEqualTo("aut-9");
        assertThat(cambio.motivo()).isEqualTo("Revision de ADN");
    }

    @Test
    void reclasificarRechazaLaMismaEspecie() {
        Hallazgo h = nuevoHallazgo();

        assertThatThrownBy(() -> h.reclasificar(especie, "x", "inv-1", "aut-9"))
                .isInstanceOf(DatosInvalidosException.class);
    }
}
