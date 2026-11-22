package co.edu.unbosque.biohub.consulta.evento;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsumidorEventosTest {

    private static final String DLQ = "consulta-publica.hallazgos.dlq";

    private final ProyectorHallazgos proyector = mock(ProyectorHallazgos.class);
    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final ConsumidorEventos consumidor =
            new ConsumidorEventos(JsonMapper.builder().build(), proyector, rabbit, DLQ);

    private static Message mensaje(String cuerpo) {
        return new Message(cuerpo.getBytes(StandardCharsets.UTF_8), new MessageProperties());
    }

    private static final String VALIDO =
            "{\"idEvento\":\"e1\",\"tipo\":\"HALLAZGO_REGISTRADO\",\"idAgregado\":\"h1\",\"datos\":{}}";

    @Test
    void eventoValidoSeAplicaYNoVaALaColaDeErrores() {
        when(proyector.aplicar(any())).thenReturn(ProyectorHallazgos.Resultado.APLICADO);
        consumidor.recibir(mensaje(VALIDO));
        verify(proyector).aplicar(any());
        verify(rabbit, never()).send(any(), any(), any(Message.class));
    }

    @Test
    void jsonInvalidoVaDirectoALaColaDeErrores() {
        Message m = mensaje("esto no es json");
        consumidor.recibir(m);
        verify(proyector, never()).aplicar(any());
        verify(rabbit).send(eq(""), eq(DLQ), eq(m));
        assertEquals(true, m.getMessageProperties().<String>getHeader(ConsumidorEventos.CABECERA_MOTIVO)
                .startsWith("JSON inválido"));
    }

    @Test
    void datosInvalidosVanALaColaDeErroresSinReintentar() {
        when(proyector.aplicar(any())).thenThrow(new EventoInvalidoException("Evento sin el campo obligatorio 'fecha'"));
        Message m = mensaje(VALIDO);
        consumidor.recibir(m); // no lanza excepción: no hay reintentos
        verify(rabbit).send(eq(""), eq(DLQ), eq(m));
        assertEquals("Evento sin el campo obligatorio 'fecha'",
                m.getMessageProperties().getHeader(ConsumidorEventos.CABECERA_MOTIVO));
    }

    @Test
    void otrosErroresSePropaganParaQueSeReintenten() {
        when(proyector.aplicar(any())).thenThrow(new IllegalStateException("base de datos caída"));
        assertThrows(IllegalStateException.class, () -> consumidor.recibir(mensaje(VALIDO)));
        verify(rabbit, never()).send(any(), any(), any(Message.class));
    }
}
