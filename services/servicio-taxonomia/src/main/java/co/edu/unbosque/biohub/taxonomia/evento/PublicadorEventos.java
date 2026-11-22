package co.edu.unbosque.biohub.taxonomia.evento;

/**
 * Publica un evento ya serializado en el broker de mensajes.
 * Los servicios no dependen del proveedor concreto (RabbitMQ, Kafka, etc.).
 */
public interface PublicadorEventos {

    void publicar(String tipo, String payload);
}
