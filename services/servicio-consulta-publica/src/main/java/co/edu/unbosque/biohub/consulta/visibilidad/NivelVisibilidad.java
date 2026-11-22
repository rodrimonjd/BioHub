package co.edu.unbosque.biohub.consulta.visibilidad;

/** Nivel de detalle de la ubicación; mismos valores que devuelve servicio-autorizacion. */
public enum NivelVisibilidad {
    EXACTA,        // coordenadas + departamento + región
    REGION,        // departamento + región, sin coordenadas
    DEPARTAMENTO   // solo el departamento
}
