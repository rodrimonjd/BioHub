package co.edu.unbosque.biohub.taxonomia.ficha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.taxonomia.atributo.FabricaAtributo;
import co.edu.unbosque.biohub.taxonomia.atributo.AtributoTaxonomico;
import co.edu.unbosque.biohub.taxonomia.atributo.TipoDato;
import co.edu.unbosque.biohub.taxonomia.compartido.DatosInvalidosException;

class FichaTest {

    private final UUID especieId = UUID.randomUUID();
    private final AtributoTaxonomico temperatura =
            FabricaAtributo.crear("temperatura del agua", TipoDato.NUMERICO, "C", null, true);
    private final AtributoTaxonomico habitat =
            FabricaAtributo.crear("habitat", TipoDato.TEXTO, null, null, false);
    private final AtributoTaxonomico dieta =
            FabricaAtributo.crear("dieta", TipoDato.LISTA, null, List.of("Insectivora", "Frugivora"), false);

    private FichaTaxonomica fichaValida() {
        return new ConstructorFicha()
                .especie(especieId)
                .autor("inv-1")
                .atributo(temperatura, "18.5")
                .atributo(habitat, null)
                .build();
    }

    @Test
    void elBuilderCreaUnaFichaActivaEnVersionUno() {
        FichaTaxonomica ficha = fichaValida();

        assertThat(ficha.getEspecieId()).isEqualTo(especieId.toString());
        assertThat(ficha.isActiva()).isTrue();
        assertThat(ficha.getVersion()).isEqualTo(1);
        assertThat(ficha.getAtributos()).hasSize(2);
        assertThat(ficha.valorDe("temperatura del agua")).isEqualTo("18.5");
        assertThat(ficha.valorDe("habitat")).isNull();
    }

    @Test
    void elBuilderExigeEspecieAutorYAlMenosUnAtributo() {
        assertThatThrownBy(() -> new ConstructorFicha().autor("inv-1").atributo(habitat, null).build())
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("especie");
        assertThatThrownBy(() -> new ConstructorFicha().especie(especieId).atributo(habitat, null).build())
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("autor");
        assertThatThrownBy(() -> new ConstructorFicha().especie(especieId).autor("inv-1").build())
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("al menos un atributo");
    }

    @Test
    void elBuilderRechazaValoresInvalidosObligatoriosVaciosYAtributosRepetidos() {
        assertThatThrownBy(() -> new ConstructorFicha().especie(especieId).autor("inv-1")
                .atributo(temperatura, "caliente").build())
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("Valor invalido");
        assertThatThrownBy(() -> new ConstructorFicha().especie(especieId).autor("inv-1")
                .atributo(temperatura, " ").build())
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("obligatorio");
        assertThatThrownBy(() -> new ConstructorFicha().especie(especieId).autor("inv-1")
                .atributo(habitat, null).atributo(habitat, "x").build())
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("ya tiene");
    }

    @Test
    void modificarValorDevuelveElAnteriorYSubeLaVersion() {
        FichaTaxonomica ficha = fichaValida();

        String anterior = ficha.modificarValor("Temperatura del agua", "21");

        assertThat(anterior).isEqualTo("18.5");
        assertThat(ficha.valorDe("temperatura del agua")).isEqualTo("21");
        assertThat(ficha.getVersion()).isEqualTo(2);
    }

    @Test
    void modificarValorPermiteLlenarYBorrarAtributosOpcionales() {
        FichaTaxonomica ficha = fichaValida();

        ficha.modificarValor("habitat", "Bosque humedo");
        assertThat(ficha.valorDe("habitat")).isEqualTo("Bosque humedo");

        ficha.modificarValor("habitat", "");
        assertThat(ficha.valorDe("habitat")).isNull();
        assertThat(ficha.getVersion()).isEqualTo(3);
    }

    @Test
    void modificarValorRechazaAtributoInexistenteValorIgualYObligatorioVacio() {
        FichaTaxonomica ficha = fichaValida();

        assertThatThrownBy(() -> ficha.modificarValor("dieta", "Insectivora"))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("no tiene el atributo");
        assertThatThrownBy(() -> ficha.modificarValor("temperatura del agua", "18.5"))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("igual");
        assertThatThrownBy(() -> ficha.modificarValor("temperatura del agua", null))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("obligatorio");
        assertThat(ficha.getVersion()).isEqualTo(1);
    }

    @Test
    void incorporarAtributoConservaLosValoresAnterioresYPuedeQuedarSinValor() {
        FichaTaxonomica ficha = fichaValida();

        ficha.incorporarAtributo(dieta, null);

        assertThat(ficha.getAtributos()).hasSize(3);
        assertThat(ficha.valorDe("dieta")).isNull();
        assertThat(ficha.valorDe("temperatura del agua")).isEqualTo("18.5");
        assertThat(ficha.getVersion()).isEqualTo(2);

        ficha.modificarValor("dieta", "Frugivora");
        assertThat(ficha.valorDe("dieta")).isEqualTo("Frugivora");
    }

    @Test
    void desactivarConservaLosDatosYBloqueaCambios() {
        FichaTaxonomica ficha = fichaValida();

        ficha.desactivar();

        assertThat(ficha.isActiva()).isFalse();
        assertThat(ficha.getVersion()).isEqualTo(2);
        assertThat(ficha.valorDe("temperatura del agua")).isEqualTo("18.5");
        assertThatThrownBy(() -> ficha.modificarValor("habitat", "x"))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("inactiva");
        assertThatThrownBy(() -> ficha.incorporarAtributo(dieta, null))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(ficha::desactivar)
                .isInstanceOf(DatosInvalidosException.class);
    }
}
