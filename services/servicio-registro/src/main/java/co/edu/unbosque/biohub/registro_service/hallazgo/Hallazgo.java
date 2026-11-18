package co.edu.unbosque.biohub.registro_service.hallazgo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import co.edu.unbosque.biohub.registro_service.evidencia.Evidencia;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "hallazgo")
public class Hallazgo {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Column(name = "investigador_id", nullable = false, length = 36)
    private String investigadorId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "especie_id")
    private Especie especie;

    @Column(name = "ficha_id", length = 36)
    private String fichaId;

    @Column(nullable = false)
    private Instant fecha;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    private Double latitud;
    private Double longitud;

    @Column(nullable = false, length = 80)
    private String departamento;

    @Column(length = 80)
    private String region;

    @Column(name = "version_actual", nullable = false)
    private int versionActual;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @OneToMany(mappedBy = "hallazgo", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Evidencia> evidencias = new ArrayList<>();

    protected Hallazgo() { }

    Hallazgo(String investigadorId, Especie especie, String fichaId, Instant fecha,
             String descripcion, Double latitud, Double longitud,
             String departamento, String region) {
        this.id = UUID.randomUUID();
        this.versionActual = 1;
        this.creadoEn = Instant.now();
        this.investigadorId = investigadorId;
        this.especie = especie;
        this.fichaId = fichaId;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.departamento = departamento;
        this.region = region;
    }

    public void agregarEvidencia(Evidencia evidencia) {
        evidencia.asignarHallazgo(this);
        this.evidencias.add(evidencia);
    }

    public String obtenerUbicacion(boolean exacta) {
        if (exacta) {
            if (latitud == null || longitud == null) {
                return "Coordenadas no registradas";
            }
            return latitud + ", " + longitud;
        }
        return region != null ? departamento + " - " + region : departamento;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInvestigadorId() {
        return investigadorId;
    }

    public void setInvestigadorId(String investigadorId) {
        this.investigadorId = investigadorId;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public String getFichaId() {
        return fichaId;
    }

    public void setFichaId(String fichaId) {
        this.fichaId = fichaId;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getVersionActual() {
        return versionActual;
    }

    public void setVersionActual(int versionActual) {
        this.versionActual = versionActual;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public List<Evidencia> getEvidencias() {
        return evidencias;
    }

    public void setEvidencias(List<Evidencia> evidencias) {
        this.evidencias = evidencias;
    }

    
}