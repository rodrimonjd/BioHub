package co.edu.unbosque.biohub.consulta.consulta;

import java.time.LocalDate;

public record FiltroHallazgos(String especieId, String departamento, String especie, String texto,
                              LocalDate fechaDesde, LocalDate fechaHasta) {

    public static final int TEXTO_MAXIMO = 100;

    public FiltroHallazgos {
        especieId = limpiar(especieId);
        departamento = limpiar(departamento);
        especie = limpiar(especie);
        texto = limpiar(texto);
    }

    public void validar() {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("fechaDesde no puede ser posterior a fechaHasta");
        }
        if (texto != null && texto.length() > TEXTO_MAXIMO) {
            throw new IllegalArgumentException("texto no puede tener más de " + TEXTO_MAXIMO + " caracteres");
        }
        if (especie != null && especie.length() > TEXTO_MAXIMO) {
            throw new IllegalArgumentException("especie no puede tener más de " + TEXTO_MAXIMO + " caracteres");
        }
    }

    private static String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
