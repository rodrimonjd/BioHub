package co.edu.unbosque.biohub.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ConsumidorExternoTest {

    private final ConsumidorExterno consumidor = new ConsumidorExterno("consumidor-unal",
            "Universidad Nacional", ConsumidorExterno.hash("clave-secreta"), 60);

    @Test
    void autenticaSoloConLaKeyCorrecta() {
        assertThat(consumidor.autenticar("clave-secreta")).isTrue();
        assertThat(consumidor.autenticar("otra-clave")).isFalse();
        assertThat(consumidor.autenticar("")).isFalse();
        assertThat(consumidor.autenticar(null)).isFalse();
    }

    @Test
    void permite60SolicitudesPorMinutoYRechazaLa61() {
        Instant ahora = Instant.parse("2026-11-22T10:00:05Z");
        for (int i = 0; i < 60; i++) {
            assertThat(consumidor.registrarSolicitud(ahora)).isTrue();
        }
        assertThat(consumidor.registrarSolicitud(ahora.plusSeconds(30))).isFalse();
        assertThat(consumidor.getSolicitudesMinutoActual()).isEqualTo(60);
    }

    @Test
    void elContadorSeReiniciaAlEmpezarElSiguienteMinuto() {
        Instant ahora = Instant.parse("2026-11-22T10:00:05Z");
        for (int i = 0; i < 60; i++) {
            consumidor.registrarSolicitud(ahora);
        }
        assertThat(consumidor.registrarSolicitud(Instant.parse("2026-11-22T10:00:59Z"))).isFalse();

        assertThat(consumidor.registrarSolicitud(Instant.parse("2026-11-22T10:01:00Z"))).isTrue();
        assertThat(consumidor.getSolicitudesMinutoActual()).isEqualTo(1);
    }

    @Test
    void calculaLosSegundosHastaElSiguienteMinuto() {
        assertThat(ConsumidorExterno.segundosParaSiguienteMinuto(Instant.parse("2026-11-22T10:00:45Z"))).isEqualTo(15);
    }

    @Test
    void cuentaLasRechazadasYLasReiniciaConElMinuto() {
        Instant ahora = Instant.parse("2026-11-22T10:00:05Z");
        for (int i = 0; i < 62; i++) {
            consumidor.registrarSolicitud(ahora);
        }

        assertThat(consumidor.solicitudesEn(ahora)).isEqualTo(60);
        assertThat(consumidor.rechazadasEn(ahora)).isEqualTo(2);
        Instant minutoSiguiente = Instant.parse("2026-11-22T10:01:00Z");
        assertThat(consumidor.solicitudesEn(minutoSiguiente)).isZero();
        assertThat(consumidor.rechazadasEn(minutoSiguiente)).isZero();
    }
}
