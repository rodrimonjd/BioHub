package co.edu.unbosque.biohub.consulta.especie;

/** Datos de una especie según servicio-registro (GET /especies). */
public record EspecieInfo(String id, String nombreCientifico, String nombreComun, String estadoConservacion) {}
