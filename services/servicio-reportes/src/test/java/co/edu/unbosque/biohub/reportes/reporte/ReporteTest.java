package co.edu.unbosque.biohub.reportes.reporte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import co.edu.unbosque.biohub.reportes.avistamiento.Avistamiento;
import co.edu.unbosque.biohub.reportes.compartido.DatosInvalidosException;

class ReporteTest {

    private static final String CAIMAN = "11111111-1111-1111-1111-111111111111";
    private static final String JAGUAR = "22222222-2222-2222-2222-222222222222";
    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIN = LocalDate.of(2026, 11, 10);

    private static Avistamiento avistamiento(String especieId, String especie, int cantidad, String departamento, String region) {
        return new Avistamiento(UUID.randomUUID().toString(), especieId, especie,
                Instant.parse("2026-11-05T10:00:00Z"), cantidad, departamento, region);
    }

    private final List<Avistamiento> datos = List.of(
            avistamiento(CAIMAN, "Crocodylus intermedius", 30, "Meta", "Orinoquia"),
            avistamiento(JAGUAR, "Panthera onca", 80, "Meta", "Orinoquia"),
            avistamiento(CAIMAN, "Crocodylus intermedius", 5, "Meta", null),
            avistamiento(JAGUAR, "Panthera onca", 2, "Amazonas", "Selva"));

    @Test
    void seCreaEnColaYValidaLosParametros() {
        Reporte reporte = new Reporte(INICIO, FIN, " ", null, "dir-1");
        assertThat(reporte.getEstado()).isEqualTo(EstadoReporte.EN_COLA);
        assertThat(reporte.getDepartamento()).isNull();

        assertThatThrownBy(() -> new Reporte(FIN, INICIO, null, null, "dir-1"))
                .isInstanceOf(DatosInvalidosException.class).hasMessageContaining("anterior");
        assertThatThrownBy(() -> new Reporte(null, FIN, null, null, "dir-1"))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> new Reporte(INICIO, FIN, null, null, " "))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void calculaTodosLosIndicesParaCadaEcosistema() {
        Reporte reporte = new Reporte(INICIO, FIN, null, null, "dir-1");

        reporte.generar(datos);

        assertThat(reporte.getEstado()).isEqualTo(EstadoReporte.LISTO);
        assertThat(reporte.getTotalAvistamientos()).isEqualTo(4);
        assertThat(reporte.getTotalIndividuos()).isEqualTo(117);
        assertThat(reporte.ecosistemas()).containsExactly("Amazonas - Selva", "Meta", "Meta - Orinoquia");
        assertThat(reporte.getIndices()).hasSize(3 * TipoIndice.values().length);

        IndicePresion densidad = reporte.indice("Meta - Orinoquia", TipoIndice.DENSIDAD_OBSERVACION);
        assertThat(densidad.getValor()).isEqualTo(11.0);        // 110 individuos / 10 dias
        assertThat(densidad.isSuperaUmbral()).isTrue();          // umbral 10
        assertThat(densidad.getIndividuos()).isEqualTo(110);

        IndicePresion riqueza = reporte.indice("Meta - Orinoquia", TipoIndice.RIQUEZA_ESPECIES);
        assertThat(riqueza.getValor()).isEqualTo(2.0);
        assertThat(riqueza.isSuperaUmbral()).isFalse();          // umbral 5
    }

    @Test
    void resumePorEspecieDeMayorAMenor() {
        Reporte reporte = new Reporte(INICIO, FIN, null, null, "dir-1");

        reporte.generar(datos);

        assertThat(reporte.getEspecies()).hasSize(2);
        ResumenEspecie jaguar = reporte.getEspecies().get(0);
        assertThat(jaguar.getEspecieId()).isEqualTo(JAGUAR);
        assertThat(jaguar.getAvistamientos()).isEqualTo(2);
        assertThat(jaguar.getIndividuos()).isEqualTo(82);
    }

    @Test
    void filtraPorDepartamentoYEspecie() {
        Reporte reporte = new Reporte(INICIO, FIN, "meta", CAIMAN, "dir-1");

        reporte.generar(datos);

        assertThat(reporte.getTotalAvistamientos()).isEqualTo(2);
        assertThat(reporte.getTotalIndividuos()).isEqualTo(35);
        assertThat(reporte.ecosistemas()).containsExactly("Meta", "Meta - Orinoquia");
        assertThat(reporte.getEspecies()).hasSize(1);
    }

    @Test
    void sinAvistamientosQuedaSinDatos() {
        Reporte reporte = new Reporte(INICIO, FIN, "Choco", null, "dir-1");

        reporte.generar(datos);

        assertThat(reporte.getEstado()).isEqualTo(EstadoReporte.SIN_DATOS);
        assertThat(reporte.getIndices()).isEmpty();
        assertThat(reporte.getEspecies()).isEmpty();
        assertThat(reporte.getMensaje()).contains("No hay avistamientos");
    }

    @Test
    void elPdfIncluyeLasSeccionesLosEcosistemasYLasEspecies() throws Exception {
        Reporte reporte = new Reporte(INICIO, FIN, null, null, "director-1");
        reporte.generar(datos);

        byte[] pdf = DocumentoPdfReporte.generar(reporte);

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        PdfReader lector = new PdfReader(pdf);
        StringBuilder texto = new StringBuilder();
        for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
            texto.append(new PdfTextExtractor(lector).getTextFromPage(pagina));
        }
        assertThat(texto.toString())
                .contains("REPORTE DE PRESIÓN SOBRE ECOSISTEMAS")
                .contains("director-1")
                .contains("01/11/2026 al 10/11/2026")
                .contains("RESUMEN GENERAL")
                .contains("Meta - Orinoquia")
                .contains("Panthera onca");
    }
}
