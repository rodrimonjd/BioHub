package co.edu.unbosque.biohub.historial.cambio;

/** El cambio no tiene todos los datos que exige la trazabilidad (autor, valores, autorizador...). */
public class RegistroCambioIncompletoException extends RuntimeException {
    public RegistroCambioIncompletoException(String mensaje) { super(mensaje); }
}
