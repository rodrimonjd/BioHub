package co.edu.unbosque.biohub.reportes.reporte;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

public final class DocumentoPdfReporte {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ServicioReportes.ZONA);

    private static final Color VERDE = new Color(31, 77, 58);
    private static final Color GRIS_CLARO = new Color(235, 240, 236);
    private static final Color ROJO = new Color(160, 40, 30);

    private static final Font TITULO_INSTITUCION = FontFactory.getFont(FontFactory.TIMES_BOLD, 14);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.TIMES_BOLD, 11);
    private static final Font TITULO_INFORME = FontFactory.getFont(FontFactory.TIMES_BOLD, 13);
    private static final Font SECCION = FontFactory.getFont(FontFactory.TIMES_BOLD, 11);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.TIMES_BOLD, 11);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11);
    private static final Font CABECERA_TABLA = FontFactory.getFont(FontFactory.TIMES_BOLD, 10, Color.WHITE);
    private static final Font CELDA = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10);
    private static final Font CELDA_ALERTA = FontFactory.getFont(FontFactory.TIMES_BOLD, 10, ROJO);
    private static final Font PIE = FontFactory.getFont(FontFactory.TIMES_ITALIC, 8, Color.GRAY);

    private DocumentoPdfReporte() { }

    public static byte[] generar(Reporte reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.LETTER, 60, 60, 50, 50);
        PdfWriter escritor = PdfWriter.getInstance(documento, salida);
        escritor.setPageEvent(new PiePagina(reporte.getId()));
        documento.addTitle("Reporte de presión sobre ecosistemas");
        documento.addAuthor("BioHub - Servicio de reportes");
        documento.open();

        encabezado(documento);
        datosIdentificacion(documento, reporte);

        seccion(documento, escritor, "1.", "OBJETIVO");
        documento.add(parrafo("Analizar la presión sobre los ecosistemas a partir de los avistamientos de fauna y flora "
                + "registrados en BioHub durante el periodo indicado, calculando para cada ecosistema los índices "
                + "de presión definidos y señalando los que superan su umbral de alerta."));

        seccion(documento, escritor, "2.", "RESUMEN GENERAL");
        resumen(documento, reporte);

        seccion(documento, escritor, "3.", "ÍNDICES DE PRESIÓN POR ECOSISTEMA");
        tablaIndices(documento, reporte);

        seccion(documento, escritor, "4.", "AVISTAMIENTOS POR ESPECIE");
        tablaEspecies(documento, reporte);

        seccion(documento, escritor, "5.", "METODOLOGÍA");
        metodologia(documento);

        seccion(documento, escritor, "6.", "CONCLUSIONES");
        conclusiones(documento, reporte);

        documento.close();
        return salida.toByteArray();
    }

    private static void encabezado(Document documento) {
        documento.add(centrado("INSTITUTO NACIONAL DE BIODIVERSIDAD", TITULO_INSTITUCION, 0));
        documento.add(centrado("BioHub — Sistema Nacional de Registro y Vigilancia de Biodiversidad", SUBTITULO, 0));
        documento.add(centrado("Servicio de reportes científicos", SUBTITULO, 6));
        documento.add(new Chunk(new LineSeparator(1.2f, 100, VERDE, Element.ALIGN_CENTER, -2)));
        documento.add(centrado("REPORTE DE PRESIÓN SOBRE ECOSISTEMAS", TITULO_INFORME, 14));
    }

    private static void datosIdentificacion(Document documento, Reporte r) {
        PdfPTable tabla = new PdfPTable(new float[] {1.3f, 3f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);
        tabla.setSpacingAfter(4);
        filaDato(tabla, "Código de reporte:", r.getId());
        filaDato(tabla, "Solicitado por:", r.getSolicitadoPor());
        filaDato(tabla, "Fecha de solicitud:", FECHA_HORA.format(r.getCreadoEn()));
        filaDato(tabla, "Fecha de generación:", r.getGeneradoEn() == null ? "-" : FECHA_HORA.format(r.getGeneradoEn()));
        filaDato(tabla, "Periodo analizado:", FECHA.format(r.getFechaInicio()) + " al " + FECHA.format(r.getFechaFin()));
        filaDato(tabla, "Departamento:", r.getDepartamento() == null ? "Todos" : r.getDepartamento());
        filaDato(tabla, "Especie:", nombreEspecieFiltro(r));
        documento.add(tabla);
    }

    private static void resumen(Document documento, Reporte r) {
        int alertas = 0;
        for (IndicePresion i : r.getIndices()) {
            if (i.isSuperaUmbral()) {
                alertas++;
            }
        }
        documento.add(vineta("Total de avistamientos analizados: ", String.valueOf(r.getTotalAvistamientos())));
        documento.add(vineta("Total de individuos observados: ", String.valueOf(r.getTotalIndividuos())));
        documento.add(vineta("Ecosistemas con registros: ", String.valueOf(r.ecosistemas().size())));
        documento.add(vineta("Especies distintas observadas: ", String.valueOf(r.getEspecies().size())));
        documento.add(vineta("Índices que superan su umbral: ", String.valueOf(alertas)));
    }

    private static void tablaIndices(Document documento, Reporte r) {
        TipoIndice[] tipos = TipoIndice.values();
        float[] anchos = new float[3 + tipos.length];
        anchos[0] = 3f;
        anchos[1] = 1.3f;
        anchos[2] = 1.2f;
        for (int i = 0; i < tipos.length; i++) {
            anchos[3 + i] = 1.6f;
        }
        PdfPTable tabla = tabla(anchos);
        cabecera(tabla, "Ecosistema");
        cabecera(tabla, "Avistamientos");
        cabecera(tabla, "Individuos");
        for (TipoIndice tipo : tipos) {
            cabecera(tabla, tipo.getNombre() + " (" + tipo.getUnidad() + ")");
        }

        boolean alterno = false;
        for (String ecosistema : r.ecosistemas()) {
            IndicePresion primero = r.indice(ecosistema, tipos[0]);
            celda(tabla, ecosistema, CELDA, alterno, Element.ALIGN_LEFT);
            celda(tabla, String.valueOf(primero.getAvistamientos()), CELDA, alterno, Element.ALIGN_CENTER);
            celda(tabla, String.valueOf(primero.getIndividuos()), CELDA, alterno, Element.ALIGN_CENTER);
            for (TipoIndice tipo : tipos) {
                IndicePresion indice = r.indice(ecosistema, tipo);
                String texto = formato(indice.getValor()) + (indice.isSuperaUmbral() ? " *" : "");
                celda(tabla, texto, indice.isSuperaUmbral() ? CELDA_ALERTA : CELDA, alterno, Element.ALIGN_CENTER);
            }
            alterno = !alterno;
        }
        documento.add(tabla);

        StringBuilder nota = new StringBuilder("* Supera el umbral de alerta. Umbrales: ");
        for (int i = 0; i < tipos.length; i++) {
            nota.append(tipos[i].getNombre()).append(" > ").append(formato(tipos[i].getUmbralPorDefecto()))
                    .append(' ').append(tipos[i].getUnidad());
            nota.append(i < tipos.length - 1 ? "; " : ".");
        }
        documento.add(new Paragraph(nota.toString(), PIE));
    }

    private static void tablaEspecies(Document documento, Reporte r) {
        PdfPTable tabla = tabla(new float[] {3.5f, 1.5f, 1.5f, 1.5f});
        cabecera(tabla, "Especie");
        cabecera(tabla, "Avistamientos");
        cabecera(tabla, "Individuos");
        cabecera(tabla, "% de individuos");
        boolean alterno = false;
        for (ResumenEspecie e : r.getEspecies()) {
            double porcentaje = r.getTotalIndividuos() == 0 ? 0 : e.getIndividuos() * 100.0 / r.getTotalIndividuos();
            celda(tabla, e.getEspecie() == null ? e.getEspecieId() : e.getEspecie(), CELDA, alterno, Element.ALIGN_LEFT);
            celda(tabla, String.valueOf(e.getAvistamientos()), CELDA, alterno, Element.ALIGN_CENTER);
            celda(tabla, String.valueOf(e.getIndividuos()), CELDA, alterno, Element.ALIGN_CENTER);
            celda(tabla, formato(porcentaje) + " %", CELDA, alterno, Element.ALIGN_CENTER);
            alterno = !alterno;
        }
        documento.add(tabla);
    }

    private static void metodologia(Document documento) {
        documento.add(parrafo("Los datos provienen de los avistamientos publicados por el servicio de registro. "
                + "Cada avistamiento se asigna a un ecosistema formado por su departamento y su región. "
                + "Para cada ecosistema se calculan los siguientes índices:"));
        for (TipoIndice tipo : TipoIndice.values()) {
            documento.add(vineta(tipo.getNombre() + ": ", tipo.getDescripcion()
                    + " Umbral de alerta: " + formato(tipo.getUmbralPorDefecto()) + " " + tipo.getUnidad() + "."));
        }
    }

    private static void conclusiones(Document documento, Reporte r) {
        List<String> enAlerta = new ArrayList<>();
        for (String ecosistema : r.ecosistemas()) {
            for (TipoIndice tipo : TipoIndice.values()) {
                IndicePresion indice = r.indice(ecosistema, tipo);
                if (indice.isSuperaUmbral()) {
                    enAlerta.add(ecosistema + " (" + tipo.getNombre().toLowerCase() + ": "
                            + formato(indice.getValor()) + " " + tipo.getUnidad() + ")");
                }
            }
        }
        if (enAlerta.isEmpty()) {
            documento.add(parrafo("Ningún ecosistema supera los umbrales de alerta definidos durante el periodo analizado."));
        } else {
            documento.add(parrafo("Los siguientes ecosistemas superan al menos un umbral de alerta y se recomienda "
                    + "priorizar su seguimiento:"));
            for (String texto : enAlerta) {
                documento.add(vineta("", texto));
            }
        }
        if (!r.getEspecies().isEmpty()) {
            ResumenEspecie principal = r.getEspecies().get(0);
            documento.add(parrafo("La especie con más individuos observados fue "
                    + (principal.getEspecie() == null ? principal.getEspecieId() : principal.getEspecie())
                    + ", con " + principal.getIndividuos() + " individuos en " + principal.getAvistamientos()
                    + " avistamiento(s)."));
        }
    }

    private static String nombreEspecieFiltro(Reporte r) {
        if (r.getEspecieId() == null) {
            return "Todas";
        }
        for (ResumenEspecie e : r.getEspecies()) {
            if (e.getEspecieId().equals(r.getEspecieId()) && e.getEspecie() != null) {
                return e.getEspecie();
            }
        }
        return r.getEspecieId();
    }

    private static void seccion(Document documento, PdfWriter escritor, String numero, String titulo) {
        if (escritor.getVerticalPosition(true) < documento.bottom() + 110) {
            documento.newPage();
        }
        Paragraph p = new Paragraph(numero + "  " + titulo, SECCION);
        p.setSpacingBefore(14);
        p.setSpacingAfter(6);
        documento.add(p);
    }

    private static Paragraph centrado(String texto, Font fuente, float espacioDespues) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(espacioDespues);
        return p;
    }

    private static Paragraph parrafo(String texto) {
        Paragraph p = new Paragraph(texto, NORMAL);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setIndentationLeft(18);
        p.setSpacingAfter(4);
        p.setLeading(15);
        return p;
    }

    private static Paragraph vineta(String etiqueta, String texto) {
        Paragraph p = new Paragraph();
        p.add(new Chunk("•  ", NORMAL));
        if (!etiqueta.isEmpty()) {
            p.add(new Chunk(etiqueta, NEGRITA));
        }
        p.add(new Chunk(texto, NORMAL));
        p.setIndentationLeft(30);
        p.setFirstLineIndent(-10);
        p.setSpacingAfter(2);
        p.setLeading(15);
        return p;
    }

    private static void filaDato(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell e = new PdfPCell(new Phrase(etiqueta, NEGRITA));
        e.setBorder(Rectangle.NO_BORDER);
        e.setPaddingBottom(4);
        PdfPCell v = new PdfPCell(new Phrase(valor, NORMAL));
        v.setBorder(Rectangle.NO_BORDER);
        v.setPaddingBottom(4);
        tabla.addCell(e);
        tabla.addCell(v);
    }

    private static PdfPTable tabla(float[] anchos) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(4);
        tabla.setSpacingAfter(4);
        tabla.setHeaderRows(1);
        return tabla;
    }

    private static void cabecera(PdfPTable tabla, String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, CABECERA_TABLA));
        c.setBackgroundColor(VERDE);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(5);
        tabla.addCell(c);
    }

    private static void celda(PdfPTable tabla, String texto, Font fuente, boolean alterno, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(4);
        if (alterno) {
            c.setBackgroundColor(GRIS_CLARO);
        }
        tabla.addCell(c);
    }

    private static String formato(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.format(java.util.Locale.US, "%.2f", valor);
    }

    private static class PiePagina extends PdfPageEventHelper {
        private final String idReporte;

        PiePagina(String idReporte) {
            this.idReporte = idReporte;
        }

        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            String texto = "BioHub · Reporte " + idReporte + " · Página " + escritor.getPageNumber()
                    + " · Generado " + FECHA_HORA.format(Instant.now());
            ColumnText.showTextAligned(escritor.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase(texto, PIE), (documento.left() + documento.right()) / 2, documento.bottom() - 20, 0);
        }
    }
}
