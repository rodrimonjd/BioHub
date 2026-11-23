package co.edu.unbosque.biohub.historial.cambio;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstructorRegistroCambioTest {

    private static final Instant FECHA = Instant.parse("2026-11-10T08:00:00.123456789Z");

    private static ConstructorRegistroCambio base(TipoCambio tipo) {
        return new ConstructorRegistroCambio()
                .evento("ev-1", "{}")
                .registro(TipoRegistro.HALLAZGO, "h1", 2)
                .tipo(tipo)
                .responsables("inv-1", null)
                .ocurrioEn(FECHA);
    }

    @Test
    void modificacionCompletaSeConstruyeConSuHash() {
        RegistroCambio c = base(TipoCambio.MODIFICACION).valores("region", null, "Llanos").construir();
        assertEquals("region", c.getCampo());
        assertEquals(64, c.getHash().length());
        assertEquals("Versión 2: region cambió de '(vacío)' a 'Llanos' por inv-1", c.describir());
    }

    @Test
    void sinAutorNoSeConstruye() {
        ConstructorRegistroCambio b = base(TipoCambio.MODIFICACION).valores("region", "A", "B").responsables(null, null);
        RegistroCambioIncompletoException e = assertThrows(RegistroCambioIncompletoException.class, b::construir);
        assertEquals("Falta el autor", e.getMessage());
    }

    @Test
    void modificacionSinCampoOValorNuevoNoSeConstruye() {
        assertThrows(RegistroCambioIncompletoException.class,
                () -> base(TipoCambio.MODIFICACION).valores(null, "A", "B").construir());
        assertThrows(RegistroCambioIncompletoException.class,
                () -> base(TipoCambio.MODIFICACION).valores("region", "A", null).construir());
    }

    @Test
    void reclasificacionExigeAutorizadorYMotivo() {
        ConstructorRegistroCambio sinAutorizador = base(TipoCambio.RECLASIFICACION)
                .valores("especie", "Ara macao", "Panthera onca").motivo("Revisión");
        RegistroCambioIncompletoException e = assertThrows(RegistroCambioIncompletoException.class, sinAutorizador::construir);
        assertEquals("Falta el autorizador de la reclasificación", e.getMessage());

        ConstructorRegistroCambio sinMotivo = base(TipoCambio.RECLASIFICACION)
                .valores("especie", "Ara macao", "Panthera onca").responsables("inv-1", "admin");
        assertThrows(RegistroCambioIncompletoException.class, sinMotivo::construir);

        RegistroCambio ok = base(TipoCambio.RECLASIFICACION).valores("especie", "Ara macao", "Panthera onca")
                .responsables("inv-1", "admin").motivo("Revisión").construir();
        assertTrue(ok.describir().contains("autorizado por admin"));
    }

    @Test
    void versionDebeSerPositiva() {
        assertThrows(RegistroCambioIncompletoException.class,
                () -> base(TipoCambio.CREACION).registro(TipoRegistro.HALLAZGO, "h1", 0).construir());
    }

    @Test
    void elHashEncadenaConElAnteriorYSePuedeRecalcular() {
        RegistroCambio primero = base(TipoCambio.CREACION).registro(TipoRegistro.HALLAZGO, "h1", 1)
                .estadoResultante("{\"a\":1}").construir();
        RegistroCambio segundo = base(TipoCambio.MODIFICACION).valores("a", "1", "2")
                .encadenarCon(primero.getHash()).estadoResultante("{\"a\":2}").construir();

        assertEquals(primero.getHash(), segundo.getHashAnterior());
        assertEquals(segundo.getHash(), ConstructorRegistroCambio.calcularHash(segundo, "{\"a\":2}"));
        // si cambia el estado guardado, el hash ya no coincide
        assertNotEquals(segundo.getHash(), ConstructorRegistroCambio.calcularHash(segundo, "{\"a\":3}"));
    }

    @Test
    void laFechaSeRecortaAMicrosegundosComoEnLaBaseDeDatos() {
        RegistroCambio c = base(TipoCambio.CREACION).construir();
        assertEquals(Instant.parse("2026-11-10T08:00:00.123456Z"), c.getOcurrioEn());
    }
}
