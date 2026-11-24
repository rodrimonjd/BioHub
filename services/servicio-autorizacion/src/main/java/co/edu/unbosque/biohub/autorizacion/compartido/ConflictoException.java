package co.edu.unbosque.biohub.autorizacion.compartido;

/** El cambio choca con el estado actual, por ejemplo un acceso que ya existe (responde 409). */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) { super(mensaje); }
}
