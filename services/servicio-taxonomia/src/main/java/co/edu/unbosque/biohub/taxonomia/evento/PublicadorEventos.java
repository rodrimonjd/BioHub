package co.edu.unbosque.biohub.taxonomia.evento;

public interface PublicadorEventos {

    void publicar(String tipo, String payload);
}
