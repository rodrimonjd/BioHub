package co.edu.unbosque.biohub.registro_service.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RetransmisorOutboxTest {

    private final OutboxEventoRepositorio repositorio = mock(OutboxEventoRepositorio.class);
    private final PublicadorEventos publicador = mock(PublicadorEventos.class);
    private final RetransmisorOutbox relay = new RetransmisorOutbox(repositorio, publicador, 50);

    private OutboxEvento evento(String tipo, String payload) {
        return new OutboxEvento(UUID.randomUUID(), tipo, "agregado", Instant.now(), payload);
    }

    @Test
    void publicaLosPendientesYLosMarca() {
        OutboxEvento primero = evento("HALLAZGO_REGISTRADO", "{\"a\":1}");
        OutboxEvento segundo = evento("AVISTAMIENTO_REGISTRADO", "{\"b\":2}");
        when(repositorio.findByPublicadoFalseOrderByOcurrioEnAsc(any())).thenReturn(List.of(primero, segundo));

        relay.publicarPendientes();

        verify(publicador).publicar("HALLAZGO_REGISTRADO", "{\"a\":1}");
        verify(publicador).publicar("AVISTAMIENTO_REGISTRADO", "{\"b\":2}");
        assertThat(primero.isPublicado()).isTrue();
        assertThat(primero.getPublicadoEn()).isNotNull();
        assertThat(segundo.isPublicado()).isTrue();
    }

    @Test
    void siElBrokerFallaSumaUnIntentoYNoSigueConLosDemas() {
        OutboxEvento primero = evento("HALLAZGO_REGISTRADO", "{\"a\":1}");
        OutboxEvento segundo = evento("HALLAZGO_MODIFICADO", "{\"b\":2}");
        when(repositorio.findByPublicadoFalseOrderByOcurrioEnAsc(any())).thenReturn(List.of(primero, segundo));
        doThrow(new RuntimeException("broker caido")).when(publicador).publicar("HALLAZGO_REGISTRADO", "{\"a\":1}");

        relay.publicarPendientes();

        assertThat(primero.isPublicado()).isFalse();
        assertThat(primero.getIntentos()).isEqualTo(1);
        assertThat(segundo.isPublicado()).isFalse();
        verify(publicador, never()).publicar("HALLAZGO_MODIFICADO", "{\"b\":2}");
    }
}
