package co.edu.unbosque.biohub.registro_service.hallazgo;

public record CambioHallazgo(
        String campo,
        String valorAnterior,
        String valorNuevo,
        String autor,
        String autorizador,
        String motivo) { }