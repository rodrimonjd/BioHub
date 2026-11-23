package co.edu.unbosque.biohub.registro_service.expedicion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.biohub.registro_service.equipo.EquipoCampo;
import co.edu.unbosque.biohub.registro_service.equipo.EquipoServicio;
import co.edu.unbosque.biohub.registro_service.evento.OutboxServicio;

class ExpedicionServicioTest {

    private final ExpedicionRepositorio expediciones = mock(ExpedicionRepositorio.class);
    private final EquipoServicio equipos = mock(EquipoServicio.class);
    private final OutboxServicio outbox = mock(OutboxServicio.class);
    private final ExpedicionServicio servicio = new ExpedicionServicio(expediciones, equipos, outbox);

    @Test
    void equipoRepetidoEs409SinEventoDuplicado() {
        Expedicion expedicion = new Expedicion("Orinoquia", "Meta", LocalDate.of(2026, 11, 1), null, "inv-1");
        EquipoCampo equipo = new EquipoCampo("CT-05", "camara trampa", null);
        when(expediciones.findById(expedicion.getId())).thenReturn(Optional.of(expedicion));
        when(equipos.obtener(equipo.getId())).thenReturn(equipo);

        servicio.asignarEquipo(expedicion.getId(), equipo.getId());

        assertThatThrownBy(() -> servicio.asignarEquipo(expedicion.getId(), equipo.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> org.assertj.core.api.Assertions
                        .assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
        verify(outbox, times(1)).guardar(any());
    }

    @Test
    void expedicionInexistenteEs404() {
        when(expediciones.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.asignarEquipo(java.util.UUID.randomUUID(), java.util.UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);
        verify(outbox, never()).guardar(any());
    }
}
