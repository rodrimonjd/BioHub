package co.edu.unbosque.biohub.registro_service.expedicion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.biohub.registro_service.equipo.EquipoCampo;
import co.edu.unbosque.biohub.registro_service.equipo.RespuestaEquipo;

public record RespuestaExpedicion(
        UUID id,
        String nombre,
        String zona,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String liderId,
        List<RespuestaEquipo> equipos) {

    public static RespuestaExpedicion de(Expedicion e) {
        List<RespuestaEquipo> equipos = new ArrayList<>();
        for (EquipoCampo equipo : e.listarEquipos()) {
            equipos.add(RespuestaEquipo.de(equipo));
        }

            return new RespuestaExpedicion( e.getId(),e.getNombre(), e.getZona(), e.getFechaInicio(), e.getFechaFin(), e.getLiderId(), equipos);
            }
}