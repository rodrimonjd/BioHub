package co.edu.unbosque.biohub.registro_service.evento;

public interface PublicadorEventos {

    void publicar(String tipo, String payload);
}
