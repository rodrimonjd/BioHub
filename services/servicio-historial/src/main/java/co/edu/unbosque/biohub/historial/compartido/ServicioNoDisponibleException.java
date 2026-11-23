package co.edu.unbosque.biohub.historial.compartido;

/** Un servicio del que depende la operación (p. ej. autorización) no respondió. */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) { super(mensaje); }
}
