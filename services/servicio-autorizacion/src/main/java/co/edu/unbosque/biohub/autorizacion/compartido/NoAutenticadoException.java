package co.edu.unbosque.biohub.autorizacion.compartido;

/** Usuario o contraseña incorrectos (HTTP 401). */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String mensaje) { super(mensaje); }
}
