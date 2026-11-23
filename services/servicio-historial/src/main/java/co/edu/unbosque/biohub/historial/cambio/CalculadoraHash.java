package co.edu.unbosque.biohub.historial.cambio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Encadena los cambios de un registro con SHA-256: el hash de cada cambio incluye el hash del anterior.
 * Si alguien altera o borra un cambio directamente en la base de datos, la cadena deja de cuadrar
 * y la verificación de integridad lo detecta (HU-19.3: "la información no ha sido eliminada ni alterada").
 */
public final class CalculadoraHash {

    private static final char SEPARADOR = '\u001F'; // separador de unidad: no aparece en datos normales

    private CalculadoraHash() {}

    public static String calcular(String hashAnterior, Object... campos) {
        StringBuilder contenido = new StringBuilder(hashAnterior == null ? "" : hashAnterior);
        for (Object campo : campos) {
            contenido.append(SEPARADOR).append(campo == null ? "" : campo.toString());
        }
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256")
                    .digest(contenido.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
