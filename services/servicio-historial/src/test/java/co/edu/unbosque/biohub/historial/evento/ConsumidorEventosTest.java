package co.edu.unbosque.biohub.historial.evento;

import co.edu.unbosque.biohub.historial.historial.ServicioRegistroHistorial;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsumidorEventosTest {

    private static final String DLQ = "historial.eventos.dlq";
    private static final String VALIDO = "{\"idEvento\":\"e1\",\"tipo\":\"HALLAZGO_REGISTRADO\",\"idAgregado\":\"h1\","
            + "\"ocurrioEn\":\"2026-11-01T10:00:00Z\",\"autor\":\"inv-1\",\"datos\":{\"version\":1}}";

    private final ServicioRegistroHistorial servicio = mock(ServicioRegistroHistorial.class);
    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final ConsumidorEventos consumidor =
            new ConsumidorEventos(JsonMapper.builder().build(), servicio, rabbit, DLQ);

    private static Message mensaje(String cuerpo) {
        return new Message(cuerpo.getBytes(StandardCharsets.UTF_8), new MessageProperties());
    }

    @Test
    void eventoValidoSeGuardaConSuCargaOriginal() {
        when(servicio.registrar(any(), anyString())).thenReturn(ServicioRegistroHistorial.Resultado.GUARDADO);
        consumidor.recibir(mensaje(VALIDO));
        verify(servicio).registrar(any(), eq(VALIDO));
        verify(rabbit, never()).send(any(), any(), any(Message.class));
    }

    @Test
    void jsonInvalidoVaALaColaDeErrores() {
        Message m = mensaje("no es json");
        consumidor.recibir(m);
        verify(servicio, never()).registrar(any(), anyString());
        verify(rabbit).send(eq(""), eq(DLQ), eq(m));
    }

    @Test
    void eventoIncompletoVaALaColaDeErroresConElMotivo() {
        when(servicio.registrar(any(), anyString())).thenThrow(new EventoInvalidoException("Evento sin autor"));
        Message m = mensaje(VALIDO);
        consumidor.recibir(m);
        verify(rabbit).send(eq(""), eq(DLQ), eq(m));
        assertEquals("Evento sin autor", m.getMessageProperties().getHeader(ConsumidorEventos.CABECERA_MOTIVO));
    }

    @Test
    void otrosErroresSePropaganParaReintentar() {
        when(servicio.registrar(any(), anyString())).thenThrow(new IllegalStateException("BD caída"));
        assertThrows(IllegalStateException.class, () -> consumidor.recibir(mensaje(VALIDO)));
        verify(rabbit, never()).send(any(), any(), any(Message.class));
    }
}
