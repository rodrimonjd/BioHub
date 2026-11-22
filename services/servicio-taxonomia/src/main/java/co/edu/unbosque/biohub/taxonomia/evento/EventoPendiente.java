package co.edu.unbosque.biohub.taxonomia.evento;

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
