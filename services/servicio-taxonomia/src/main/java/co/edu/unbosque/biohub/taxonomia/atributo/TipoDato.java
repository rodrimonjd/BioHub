package co.edu.unbosque.biohub.taxonomia.atributo;

import java.util.List;

public enum TipoDato {

    TEXTO {
        @Override
        public boolean esValido(String valor, List<String> opciones) {
            return valor.length() <= 500;
        }
    },

    NUMERICO {
        @Override
        public boolean esValido(String valor, List<String> opciones) {
            try {
                double numero = Double.parseDouble(valor);
                return !Double.isNaN(numero) && !Double.isInfinite(numero);
            } catch (NumberFormatException e) {
                return false;
            }
        }
    },

    LISTA {
        @Override
        public boolean esValido(String valor, List<String> opciones) {
            if (opciones == null) {
                return false;
            }
            for (String opcion : opciones) {
                if (opcion.equalsIgnoreCase(valor)) {
                    return true;
                }
            }
            return false;
        }
    };

    public abstract boolean esValido(String valor, List<String> opciones);
}
