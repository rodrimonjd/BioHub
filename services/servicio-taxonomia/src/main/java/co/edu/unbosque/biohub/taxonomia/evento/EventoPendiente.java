package co.edu.unbosque.biohub.taxonomia.evento;

/**
 * Evento guardado dentro de la ficha hasta que se publique en RabbitMQ.
 * payload es el EventoDominio convertido a JSON.
 */
public class EventoPendiente {

    private String tipo;
    private String payload;

    protected EventoPendiente() { }

    public EventoPendiente(String tipo, String payload) {
        this.tipo = tipo;
        this.payload = payload;
    }

    public String getTipo() { return tipo; }
    public String getPayload() { return payload; }
}
