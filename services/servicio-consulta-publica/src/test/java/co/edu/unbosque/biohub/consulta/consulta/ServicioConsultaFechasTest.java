package co.edu.unbosque.biohub.consulta.consulta;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unbosque.biohub.consulta.especie.CatalogoEspecies;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLecturaRepositorio;
import co.edu.unbosque.biohub.consulta.visibilidad.ClienteAutorizacion;

class ServicioConsultaFechasTest {

    private final HallazgoLecturaRepositorio repositorio = mock(HallazgoLecturaRepositorio.class);
    private final ServicioConsulta servicio = new ServicioConsulta(repositorio, mock(CatalogoEspecies.class),
            mock(ClienteAutorizacion.class));

    @Test
    void lasFechasSonDiasCompletosEnHoraDeColombia() {
        when(repositorio.buscar(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(Page.empty());

        servicio.listar(null, null, null, null, LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"), 0, 20);

        // Colombia es UTC-5: el 1 de noviembre empieza a las 05:00 UTC y el 30 termina antes del 1 de diciembre 05:00 UTC
        verify(repositorio).buscar(isNull(), isNull(), isNull(),
                eq(Instant.parse("2026-11-01T05:00:00Z")), eq(Instant.parse("2026-12-01T05:00:00Z")), any(Pageable.class));
    }

    @Test
    void unRangoInvertidoSeRechaza() {
        assertThatThrownBy(() -> servicio.listar(null, null, null, null,
                LocalDate.parse("2026-11-30"), LocalDate.parse("2026-11-01"), 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fechaDesde");
    }
}
