package co.edu.unbosque.biohub.registro_service.evento;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.registro_service.equipo.EquipoCampo;
import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.expedicion.Expedicion;
import co.edu.unbosque.biohub.registro_service.hallazgo.Hallazgo;
import co.edu.unbosque.biohub.registro_service.hallazgo.HallazgoBuilder;

class EventoFactoryTest {

    private final Especie especie = new Especie("Atelopus varius", "Rana arlequin", "CR");
    private final Expedicion expedicion =
            new Expedicion("Orinoquia", "Meta", LocalDate.of(2026, 11, 1), null, "inv-1");

    private HallazgoBuilder builder() {
        return new HallazgoBuilder()
                .investigador("inv-1")
                .especie(especie)
                .fecha(Instant.now())
                .descripcion("x")
                .ubicacion(null, null, "Meta", null);
    }

    @Test
    void hallazgoRegistradoSinExpedicionNoIncluyeElCampo() {
        EventoDominio evento = EventoFactory.hallazgoRegistrado(builder().build());

        assertThat(evento.tipo()).isEqualTo(EventoDominio.HALLAZGO_REGISTRADO);
        assertThat(evento.autor()).isEqualTo("inv-1");
        assertThat(evento.datos()).doesNotContainKey("expedicionId");
    }

    @Test
    void hallazgoRegistradoConExpedicionIncluyeSuId() {
        Hallazgo h = builder().expedicion(expedicion).build();

        EventoDominio evento = EventoFactory.hallazgoRegistrado(h);

        assertThat(evento.idAgregado()).isEqualTo(h.getId().toString());
        assertThat(evento.datos()).containsEntry("expedicionId", expedicion.getId());
    }

    @Test
    void equipoAsignadoLlevaElResponsableYElLiderComoAutor() {
        EquipoCampo equipo = new EquipoCampo("CT-01", "camara trampa", null);
        expedicion.asignarEquipo(equipo);

        EventoDominio evento = EventoFactory.equipoAsignado(expedicion, equipo);

        assertThat(evento.tipo()).isEqualTo(EventoDominio.EQUIPO_ASIGNADO);
        assertThat(evento.idAgregado()).isEqualTo(expedicion.getId().toString());
        assertThat(evento.autor()).isEqualTo("inv-1");
        assertThat(evento.datos()).containsEntry("equipoId", equipo.getId())
                .containsEntry("responsableId", "inv-1");
    }
}
