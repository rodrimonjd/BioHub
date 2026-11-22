package co.edu.unbosque.biohub.taxonomia.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.taxonomia.atributo.FabricaAtributo;
import co.edu.unbosque.biohub.taxonomia.atributo.TipoDato;
import co.edu.unbosque.biohub.taxonomia.ficha.ConstructorFicha;
import co.edu.unbosque.biohub.taxonomia.ficha.FichaRepositorio;
import co.edu.unbosque.biohub.taxonomia.ficha.FichaTaxonomica;

class RetransmisorEventosTest {

    private final FichaRepositorio fichas = mock(FichaRepositorio.class);
    private final PublicadorEventos publicador = mock(PublicadorEventos.class);
    private final RetransmisorEventos relay = new RetransmisorEventos(fichas, publicador);

    private FichaTaxonomica fichaConEventos(String... tipos) {
        FichaTaxonomica ficha = new ConstructorFicha()
                .especie(UUID.randomUUID())
                .autor("inv-1")
                .atributo(FabricaAtributo.crear("habitat", TipoDato.TEXTO, null, null, false), null)
                .build();
        for (String tipo : tipos) {
            ficha.agregarEvento(new EventoPendiente(tipo, "{\"tipo\":\"" + tipo + "\"}"));
        }
        return ficha;
    }

    @Test
    void publicaLosEventosEnOrdenYLosQuitaDeLaFicha() {
        FichaTaxonomica ficha = fichaConEventos("FICHA_CREADA", "FICHA_MODIFICADA");
        when(fichas.buscarConEventosPendientes()).thenReturn(List.of(ficha));

        relay.publicarPendientes();

        verify(publicador).publicar("FICHA_CREADA", "{\"tipo\":\"FICHA_CREADA\"}");
        verify(publicador).publicar("FICHA_MODIFICADA", "{\"tipo\":\"FICHA_MODIFICADA\"}");
        assertThat(ficha.getEventosPendientes()).isEmpty();
        verify(fichas).save(ficha);
    }

    @Test
    void siRabbitFallaDejaElEventoPendienteYSeDetiene() {
        FichaTaxonomica primera = fichaConEventos("FICHA_CREADA", "FICHA_MODIFICADA");
        FichaTaxonomica segunda = fichaConEventos("FICHA_DESACTIVADA");
        when(fichas.buscarConEventosPendientes()).thenReturn(List.of(primera, segunda));
        doThrow(new RuntimeException("broker caido"))
                .when(publicador).publicar("FICHA_CREADA", "{\"tipo\":\"FICHA_CREADA\"}");

        relay.publicarPendientes();

        assertThat(primera.getEventosPendientes()).hasSize(2);
        verify(publicador, never()).publicar("FICHA_MODIFICADA", "{\"tipo\":\"FICHA_MODIFICADA\"}");
        verify(publicador, never()).publicar("FICHA_DESACTIVADA", "{\"tipo\":\"FICHA_DESACTIVADA\"}");
        verify(fichas, never()).save(primera);
    }
}
