package co.edu.unbosque.biohub.historial.evento;

public class EventoInvalidoException extends RuntimeException {
    public EventoInvalidoException(String mensaje) { super(mensaje); }
    public EventoInvalidoException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
