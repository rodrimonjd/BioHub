package co.edu.unbosque.biohub.taxonomia.atributo;

import java.util.ArrayList;
import java.util.List;

import co.edu.unbosque.biohub.taxonomia.compartido.DatosInvalidosException;

public final class FabricaAtributo {

    private FabricaAtributo() { }

    public static AtributoTaxonomico crear(String nombre, TipoDato tipoDato, String unidad, List<String> opciones, boolean obligatorio) {

        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre del atributo es obligatorio");
        }
        if (tipoDato == null) {
            throw new DatosInvalidosException("El tipo de dato es obligatorio (TEXTO, NUMERICO o LISTA)");
        }
        String nombreLimpio = nombre.trim();

        if (tipoDato == TipoDato.TEXTO) {
            return new AtributoTaxonomico(nombreLimpio, tipoDato, null, null, obligatorio);
        }
        if (tipoDato == TipoDato.NUMERICO) {
            String unidadLimpia = (unidad == null || unidad.isBlank()) ? null : unidad.trim();
            return new AtributoTaxonomico(nombreLimpio, tipoDato, unidadLimpia, null, obligatorio);
        }

        List<String> opcionesLimpias = new ArrayList<>();
        if (opciones != null) {
            for (String opcion : opciones) {
                if (opcion != null && !opcion.isBlank()) {
                    opcionesLimpias.add(opcion.trim());
                }
            }
        }
        if (opcionesLimpias.isEmpty()) {
            throw new DatosInvalidosException("Un atributo de tipo LISTA requiere al menos una opcion");
        }
        return new AtributoTaxonomico(nombreLimpio, tipoDato, null, opcionesLimpias, obligatorio);
    }
}
