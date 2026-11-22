package co.edu.unbosque.biohub.registro_service.expedicion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.biohub.registro_service.equipo.EquipoCampo;
import co.edu.unbosque.biohub.registro_service.equipo.EquipoResponse;

public record ExpedicionResponse(
        UUID id,
        String nombre,
        String zona,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String liderId,
        List<EquipoResponse> equipos) {

    public static ExpedicionResponse de(Expedicion e) {
        List<EquipoResponse> equipos = new ArrayList<>();
        for (EquipoCampo equipo : e.listarEquipos()) {
            equipos.add(EquipoResponse.de(equipo));
        }

            return new ExpedicionResponse( e.getId(),e.getNombre(), e.getZona(), e.getFechaInicio(), e.getFechaFin(), e.getLiderId(), equipos);
            }
}