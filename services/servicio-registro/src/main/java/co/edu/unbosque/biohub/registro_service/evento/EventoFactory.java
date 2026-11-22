package co.edu.unbosque.biohub.registro_service.evento;

import co.edu.unbosque.biohub.registro_service.avistamiento.Avistamiento;
import co.edu.unbosque.biohub.registro_service.equipo.EquipoCampo;
import co.edu.unbosque.biohub.registro_service.expedicion.Expedicion;
import co.edu.unbosque.biohub.registro_service.hallazgo.CambioHallazgo;
import co.edu.unbosque.biohub.registro_service.hallazgo.Hallazgo;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class EventoFactory {

    private static final int VERSION_ESQUEMA = 1;

    private EventoFactory() { }

    public static EventoDominio hallazgoRegistrado(Hallazgo h) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("especieId", h.getEspecie().getId());
        datos.put("especie", h.getEspecie().getNombreCientifico());
        datos.put("fichaId", h.getFichaId());
        datos.put("fecha", h.getFecha());
        datos.put("descripcion", h.getDescripcion());
        datos.put("latitud", h.getLatitud());
        datos.put("longitud", h.getLongitud());
        datos.put("departamento", h.getDepartamento());
        datos.put("region", h.getRegion());
        datos.put("version", h.getVersionActual());
        datos.put("totalEvidencias", h.getEvidencias().size());
        if (h.getExpedicion() != null) {
            datos.put("expedicionId", h.getExpedicion().getId());
        }
        return crear(EventoDominio.HALLAZGO_REGISTRADO, h.getId().toString(),
                h.getInvestigadorId(), null, datos);
    }

    public static EventoDominio hallazgoModificado(Hallazgo h, CambioHallazgo c) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("campo", c.campo());
        datos.put("valorAnterior", c.valorAnterior());
        datos.put("valorNuevo", c.valorNuevo());
        datos.put("motivo", c.motivo());
        datos.put("version", h.getVersionActual());
        return crear(EventoDominio.HALLAZGO_MODIFICADO, h.getId().toString(),
                c.autor(), c.autorizador(), datos);
    }

    public static EventoDominio avistamientoRegistrado(Avistamiento a) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("especieId", a.getEspecie().getId());
        datos.put("especie", a.getEspecie().getNombreCientifico());
        datos.put("fechaHora", a.getFechaHora());
        datos.put("cantidad", a.getCantidad());
        datos.put("latitud", a.getLatitud());
        datos.put("longitud", a.getLongitud());
        datos.put("departamento", a.getDepartamento());
        datos.put("region", a.getRegion());
        return crear(EventoDominio.AVISTAMIENTO_REGISTRADO, a.getId().toString(),
                a.getInvestigadorId(), null, datos);
    }

    public static EventoDominio equipoAsignado(Expedicion e, EquipoCampo equipo) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("equipoId", equipo.getId());
        datos.put("numeroSerie", equipo.getNumeroSerie());
        datos.put("tipoEquipo", equipo.getTipo());
        datos.put("responsableId", equipo.getResponsableId());
        return crear(EventoDominio.EQUIPO_ASIGNADO, e.getId().toString(),
                e.getLiderId(), null, datos);
    }

    private static EventoDominio crear(String tipo, String idAgregado, String autor,
                                       String autorizador, Map<String, Object> datos) {
        return new EventoDominio(UUID.randomUUID(), tipo, VERSION_ESQUEMA,
                idAgregado, Instant.now(), autor, autorizador, datos);
    }
}