package co.edu.unbosque.biohub.reportes.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import co.edu.unbosque.biohub.reportes.avistamiento.Avistamiento;
import co.edu.unbosque.biohub.reportes.avistamiento.AvistamientoRepositorio;
import co.edu.unbosque.biohub.reportes.evento.ConsumidorAvistamientos.EventoEntrante;
import tools.jackson.databind.json.JsonMapper;

class ConsumidorAvistamientosTest {

    private final AvistamientoRepositorio repositorio = mock(AvistamientoRepositorio.class);
    private final ConsumidorAvistamientos consumidor =
            new ConsumidorAvistamientos(JsonMapper.builder().build(), repositorio);

    private static Map<String, Object> datos(Object fechaHora) {
        Map<String, Object> d = new HashMap<>();
        d.put("especieId", "33333333-3333-3333-3333-333333333333");
        d.put("especie", "Ara macao");
        d.put("fechaHora", fechaHora);
        d.put("cantidad", 4);
        d.put("departamento", "Meta");
        d.put("region", "Orinoquia");
        return d;
    }

    @Test
    void guardaElAvistamientoDelEvento() {
        consumidor.procesar(new EventoEntrante("e1", "AVISTAMIENTO_REGISTRADO", "av-1",
                datos("2026-11-20T10:00:00Z")));

        ArgumentCaptor<Avistamiento> guardado = ArgumentCaptor.forClass(Avistamiento.class);
        verify(repositorio).save(guardado.capture());
        assertThat(guardado.getValue().getId()).isEqualTo("av-1");
        assertThat(guardado.getValue().getCantidad()).isEqualTo(4);
        assertThat(guardado.getValue().getFechaHora()).isEqualTo(Instant.parse("2026-11-20T10:00:00Z"));
        assertThat(guardado.getValue().ecosistema()).isEqualTo("Meta - Orinoquia");
    }

    @Test
    void aceptaLaFechaComoSegundosDesde1970() {
        consumidor.procesar(new EventoEntrante("e1", "AVISTAMIENTO_REGISTRADO", "av-2", datos(1795176000.5)));

        ArgumentCaptor<Avistamiento> guardado = ArgumentCaptor.forClass(Avistamiento.class);
        verify(repositorio).save(guardado.capture());
        assertThat(guardado.getValue().getFechaHora().getEpochSecond()).isEqualTo(1795176000L);
    }

    @Test
    void ignoraEventosRepetidosYDeOtroTipo() {
        when(repositorio.existsById("av-1")).thenReturn(true);

        consumidor.procesar(new EventoEntrante("e1", "AVISTAMIENTO_REGISTRADO", "av-1", datos("2026-11-20T10:00:00Z")));
        consumidor.procesar(new EventoEntrante("e2", "HALLAZGO_REGISTRADO", "h-1", datos("2026-11-20T10:00:00Z")));

        verify(repositorio, never()).save(any());
    }

    @Test
    void unEventoIncompletoFallaParaQueTermineEnLaColaDeErrores() {
        Map<String, Object> sinDepartamento = datos("2026-11-20T10:00:00Z");
        sinDepartamento.remove("departamento");

        assertThatThrownBy(() -> consumidor.procesar(
                new EventoEntrante("e1", "AVISTAMIENTO_REGISTRADO", "av-3", sinDepartamento)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("departamento");
        verify(repositorio, never()).save(any());
    }
}
