package co.edu.unbosque.biohub.consulta.evento;

/**
 * El evento tiene datos que nunca se van a poder procesar (falta un campo, fecha mal escrita...).
 * Reintentarlo no sirve: se manda directamente a la cola de errores.
 */
public class EventoInvalidoException extends RuntimeException {
    public EventoInvalidoException(String mensaje) { super(mensaje); }
    public EventoInvalidoException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
