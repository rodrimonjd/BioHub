package co.edu.unbosque.biohub.registro_service.hallazgo;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.evidencia.DatosInvalidosException;
import co.edu.unbosque.biohub.registro_service.evidencia.Evidencia;
import co.edu.unbosque.biohub.registro_service.expedicion.Expedicion;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HallazgoBuilder {

    private String investigadorId;
    private Especie especie;
    private String fichaId;
    private Instant fecha;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String departamento;
    private String region;
    private Expedicion expedicion;
    private final List<Evidencia> evidencias = new ArrayList<>();

    public HallazgoBuilder investigador(String investigadorId) {
        this.investigadorId = investigadorId;
        return this;
    }

    public HallazgoBuilder especie(Especie especie) {
        this.especie = especie;
        return this;
    }

    public HallazgoBuilder ficha(String fichaId) {
        this.fichaId = fichaId;
        return this;
    }

    public HallazgoBuilder fecha(Instant fecha) {
        this.fecha = fecha;
        return this;
    }

    public HallazgoBuilder descripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public HallazgoBuilder ubicacion(Double latitud, Double longitud,
                                     String departamento, String region) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.departamento = departamento;
        this.region = region;
        return this;
    }

    public HallazgoBuilder expedicion(Expedicion expedicion) {
        this.expedicion = expedicion;
        return this;
    }

    public HallazgoBuilder agregarEvidencia(Evidencia evidencia) {
        this.evidencias.add(evidencia);
        return this;
    }

    public Hallazgo build() {
    exigirTexto(investigadorId, "El investigador responsable es obligatorio");
    exigir(especie != null, "La especie es obligatoria");
    exigir(fecha != null, "La fecha es obligatoria");
    exigir(!fecha.isAfter(Instant.now()), "La fecha no puede estar en el futuro");
    exigirTexto(descripcion, "La descripcion es obligatoria");
    exigirTexto(departamento, "El departamento es obligatorio");
    exigir((latitud == null) == (longitud == null),
            "Latitud y longitud deben venir juntas o ninguna");

    Hallazgo hallazgo = new Hallazgo(investigadorId, especie, fichaId, fecha,
            descripcion, latitud, longitud, departamento, region);
    if (expedicion != null) {
        hallazgo.asignarExpedicion(expedicion);
    }
    for (Evidencia evidencia : evidencias) {
        hallazgo.agregarEvidencia(evidencia);
    }
    return hallazgo;
}

private static void exigir(boolean condicion, String mensaje) {
    if (!condicion) {
        throw new DatosInvalidosException(mensaje);
    }
}

private static void exigirTexto(String texto, String mensaje) {
    exigir(texto != null && !texto.isBlank(), mensaje);
}
}