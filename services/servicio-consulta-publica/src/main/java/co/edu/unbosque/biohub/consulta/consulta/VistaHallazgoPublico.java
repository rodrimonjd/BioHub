package co.edu.unbosque.biohub.consulta.consulta;

import co.edu.unbosque.biohub.consulta.visibilidad.NivelVisibilidad;

import java.time.Instant;

/**
 * Hallazgo tal como se muestra al consumidor. Según {@code nivelUbicacion}:
 * EXACTA trae coordenadas; REGION trae departamento y región; DEPARTAMENTO solo el departamento.
 * Los campos ocultos van en null.
 */
public record VistaHallazgoPublico(
        String id,
        String especieId,
        String especie,
        String nombreComun,
        String categoriaConservacion,
        Instant fecha,
        String descripcion,
        String departamento,
        String region,
        Double latitud,
        Double longitud,
        NivelVisibilidad nivelUbicacion) {}
