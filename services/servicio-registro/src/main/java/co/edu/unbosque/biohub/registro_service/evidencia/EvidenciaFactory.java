package co.edu.unbosque.biohub.registro_service.evidencia;

public final class EvidenciaFactory {

    private EvidenciaFactory() { }

    public static Evidencia crear(TipoEvidencia tipo, String valor, String unidad,
                                  String urlArchivo, String atributoTaxonomico) {
        if (tipo == null) {
            throw new DatosInvalidosException("El tipo de evidencia es obligatorio");
        }
        return switch (tipo) {
            case TEMPERATURA -> crearTemperatura(valor, unidad, atributoTaxonomico);
            case ADN -> crearAdn(valor, atributoTaxonomico);
            case AUDIO -> crearAudio(urlArchivo, atributoTaxonomico);
        };
    }

    private static Evidencia crearTemperatura(String valor, String unidad, String atributo) {
        if (esVacio(valor)) {
            throw new DatosInvalidosException("La temperatura requiere un valor");
        }
        try {
            Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El valor de la temperatura debe ser numerico: " + valor);
        }
        if (esVacio(unidad)) {
            throw new DatosInvalidosException("La temperatura requiere una unidad (por ejemplo, C)");
        }
        return new Evidencia(TipoEvidencia.TEMPERATURA, valor.trim(), unidad.trim(), null, atributo);
    }

    private static Evidencia crearAdn(String valor, String atributo) {
        if (esVacio(valor)) {
            throw new DatosInvalidosException("La evidencia de ADN requiere el codigo de la muestra");
        }
        return new Evidencia(TipoEvidencia.ADN, valor.trim(), null, null, atributo);
    }

    private static Evidencia crearAudio(String urlArchivo, String atributo) {
        if (esVacio(urlArchivo)) {
            throw new DatosInvalidosException("La evidencia de audio requiere la URL del archivo");
        }
        return new Evidencia(TipoEvidencia.AUDIO, null, null, urlArchivo.trim(), atributo);
    }

    private static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}