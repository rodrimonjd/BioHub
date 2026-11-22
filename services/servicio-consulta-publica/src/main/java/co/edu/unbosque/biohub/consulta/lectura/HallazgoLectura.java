package co.edu.unbosque.biohub.consulta.lectura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "hallazgo_lectura")
public class HallazgoLectura {

    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "especie_id", length = 36)
    private String especieId;
    @Column(nullable = false, length = 150)
    private String especie;
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
    @Column(nullable = false)
    private int version;
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    protected HallazgoLectura() {}

    public HallazgoLectura(String id) {
        this.id = id;
    }

    public void registrar(String especieId, String especie, Instant fecha, String descripcion,
                          Double latitud, Double longitud, String departamento, String region, int version) {
        this.especieId = especieId;
        this.especie = especie;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.departamento = departamento;
        this.region = region;
        marcarVersion(version);
    }

    public void cambiarDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void cambiarDepartamento(String departamento) { this.departamento = departamento; }
    public void cambiarRegion(String region) { this.region = region; }
    public void reclasificar(String especieId, String especie) {
        this.especieId = especieId;
        this.especie = especie;
    }

    public void marcarVersion(int version) {
        this.version = version;
        this.actualizadoEn = Instant.now();
    }

    public String getId() { return id; }
    public String getEspecieId() { return especieId; }
    public String getEspecie() { return especie; }
    public Instant getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public String getDepartamento() { return departamento; }
    public String getRegion() { return region; }
    public int getVersion() { return version; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
