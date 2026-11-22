package co.edu.unbosque.biohub.consulta.consulta;

import co.edu.unbosque.biohub.consulta.visibilidad.NivelVisibilidad;

import java.time.Instant;

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
