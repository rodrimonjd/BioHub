package co.edu.unbosque.biohub.taxonomia.atributo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.taxonomia.compartido.DatosInvalidosException;

class AtributoTest {

    @Test
    void laFabricaCreaCadaTipoConSusReglas() {
        AtributoTaxonomico texto = FabricaAtributo.crear("  habitat ", TipoDato.TEXTO, "m", List.of("x"), false);
        AtributoTaxonomico numero = FabricaAtributo.crear("temperatura del agua", TipoDato.NUMERICO, " C ", null, true);
        AtributoTaxonomico lista = FabricaAtributo.crear("dieta", TipoDato.LISTA, "kg",
                Arrays.asList(" Insectivora ", "", null, "Frugivora"), false);

        assertThat(texto.getNombre()).isEqualTo("habitat");
        assertThat(texto.getUnidad()).isNull();
        assertThat(texto.getOpciones()).isNull();
        assertThat(numero.getUnidad()).isEqualTo("C");
        assertThat(numero.isObligatorio()).isTrue();
        assertThat(lista.getOpciones()).containsExactly("Insectivora", "Frugivora");
        assertThat(lista.getUnidad()).isNull();
    }

    @Test
    void laFabricaRechazaListaSinOpcionesNombreVacioYTipoNulo() {
        assertThatThrownBy(() -> FabricaAtributo.crear("dieta", TipoDato.LISTA, null, List.of(" "), false))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("LISTA");
        assertThatThrownBy(() -> FabricaAtributo.crear(" ", TipoDato.TEXTO, null, null, false))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> FabricaAtributo.crear("habitat", null, null, null, false))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void cadaTipoValidaSusValores() {
        AtributoTaxonomico numero = FabricaAtributo.crear("peso", TipoDato.NUMERICO, "g", null, false);
        AtributoTaxonomico lista = FabricaAtributo.crear("dieta", TipoDato.LISTA, null, List.of("Insectivora"), false);
        AtributoTaxonomico texto = FabricaAtributo.crear("habitat", TipoDato.TEXTO, null, null, false);

        assertThat(numero.validarValor("12.5")).isTrue();
        assertThat(numero.validarValor("-3")).isTrue();
        assertThat(numero.validarValor("doce")).isFalse();
        assertThat(numero.validarValor("NaN")).isFalse();
        assertThat(lista.validarValor("insectivora")).isTrue();
        assertThat(lista.validarValor("Carnivora")).isFalse();
        assertThat(texto.validarValor("Bosque humedo")).isTrue();
        assertThat(texto.validarValor("x".repeat(501))).isFalse();
    }

    @Test
    void unValorVacioSoloEsValidoSiElAtributoNoEsObligatorio() {
        AtributoTaxonomico opcional = FabricaAtributo.crear("habitat", TipoDato.TEXTO, null, null, false);
        AtributoTaxonomico obligatorio = FabricaAtributo.crear("peso", TipoDato.NUMERICO, "g", null, true);

        assertThat(opcional.validarValor(null)).isTrue();
        assertThat(opcional.validarValor("  ")).isTrue();
        assertThat(obligatorio.validarValor(null)).isFalse();
        assertThat(obligatorio.validarValor("")).isFalse();
    }
}
