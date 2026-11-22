package co.edu.unbosque.biohub.taxonomia.evento;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.taxonomia.atributo.AtributoFactory;
import co.edu.unbosque.biohub.taxonomia.atributo.TipoDato;
import co.edu.unbosque.biohub.taxonomia.ficha.FichaBuilder;
import co.edu.unbosque.biohub.taxonomia.ficha.FichaTaxonomica;

class EventoFactoryTest {

    private FichaTaxonomica ficha() {
        return new FichaBuilder()
                .especie(UUID.randomUUID())
                .autor("inv-1")
                .atributo(AtributoFactory.crear("peso", TipoDato.NUMERICO, "g", null, true), "12")
                .build();
    }

    @Test
    void fichaCreadaIncluyeEspecieYValores() {
        FichaTaxonomica f = ficha();

        EventoDominio evento = EventoFactory.fichaCreada(f);

        assertThat(evento.tipo()).isEqualTo(EventoDominio.FICHA_CREADA);
        assertThat(evento.idAgregado()).isEqualTo(f.getId());
        assertThat(evento.autor()).isEqualTo("inv-1");
        assertThat(evento.datos()).containsEntry("especieId", f.getEspecieId());
        assertThat(evento.datos().get("atributos")).isEqualTo(Map.of("peso", "12"));
    }

    @Test
    void fichaModificadaYDesactivadaLlevanAutorVersionYDetalle() {
        FichaTaxonomica f = ficha();
        String anterior = f.modificarValor("peso", "15");

        EventoDominio modificada = EventoFactory.fichaModificada(f, "peso", anterior, "15", "inv-2");
        assertThat(modificada.tipo()).isEqualTo(EventoDominio.FICHA_MODIFICADA);
        assertThat(modificada.autor()).isEqualTo("inv-2");
        assertThat(modificada.datos())
                .containsEntry("campo", "peso")
                .containsEntry("valorAnterior", "12")
                .containsEntry("valorNuevo", "15")
                .containsEntry("version", 2);

        f.desactivar();
        EventoDominio desactivada = EventoFactory.fichaDesactivada(f, "aut-1", "Especie reclasificada");
        assertThat(desactivada.tipo()).isEqualTo(EventoDominio.FICHA_DESACTIVADA);
        assertThat(desactivada.datos()).containsEntry("motivo", "Especie reclasificada").containsEntry("version", 3);
    }
}
