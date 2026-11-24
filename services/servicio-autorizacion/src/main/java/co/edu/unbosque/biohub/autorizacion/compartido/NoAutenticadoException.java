package co.edu.unbosque.biohub.autorizacion.compartido;

public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String mensaje) { super(mensaje); }
}
