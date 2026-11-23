package co.edu.unbosque.biohub.historial.evento;

/** El evento no se puede convertir en un cambio válido; reintentarlo no sirve, va a la cola de errores. */
public class EventoInvalidoException extends RuntimeException {
    public EventoInvalidoException(String mensaje) { super(mensaje); }
    public EventoInvalidoException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
