package co.edu.unbosque.biohub.registro_service.evidencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EvidenciaTest {

    @Test
    void laFabricaCreaEvidenciasValidasDeCadaTipo() {
        Evidencia temperatura = EvidenciaFactory.crear(TipoEvidencia.TEMPERATURA, " 18.5 ", "C", null, null);
        Evidencia adn = EvidenciaFactory.crear(TipoEvidencia.ADN, "M-001", null, null, null);
        Evidencia audio = EvidenciaFactory.crear(TipoEvidencia.AUDIO, null, null, "https://x/canto.wav", null);

        assertThat(temperatura.getValor()).isEqualTo("18.5");
        assertThat(temperatura.validarFormato()).isTrue();
        assertThat(adn.validarFormato()).isTrue();
        assertThat(audio.validarFormato()).isTrue();
    }

    @Test
    void laFabricaRechazaTemperaturaNoNumerica() {
        assertThatThrownBy(() -> EvidenciaFactory.crear(TipoEvidencia.TEMPERATURA, "caliente", "C", null, null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("numerico");
    }

    @Test
    void laFabricaRechazaAudioSinUrl() {
        assertThatThrownBy(() -> EvidenciaFactory.crear(TipoEvidencia.AUDIO, null, null, " ", null))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void laFabricaRechazaTipoNulo() {
        assertThatThrownBy(() -> EvidenciaFactory.crear(null, "x", null, null, null))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void validarFormatoDetectaEvidenciasIncompletas() {
        assertThat(new Evidencia(TipoEvidencia.TEMPERATURA, "18", null, null, null).validarFormato()).isFalse();
        assertThat(new Evidencia(TipoEvidencia.TEMPERATURA, "abc", "C", null, null).validarFormato()).isFalse();
        assertThat(new Evidencia(TipoEvidencia.ADN, null, null, null, null).validarFormato()).isFalse();
        assertThat(new Evidencia(TipoEvidencia.AUDIO, null, null, null, null).validarFormato()).isFalse();
        assertThat(new Evidencia(null, "x", null, null, null).validarFormato()).isFalse();
    }
}
