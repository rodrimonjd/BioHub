package co.edu.unbosque.biohub.historial.cambio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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
