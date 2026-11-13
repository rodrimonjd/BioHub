package co.edu.unbosque.biohub.registro_service.avistamiento;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unbosque.biohub.registro_service.especie.Especie;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "avistamiento")
public class Avistamiento {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Column(name = "investigador_id", nullable = false, length = 36)
    private String investigadorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "especie_id")
    private Especie especie;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(nullable = false)
    private int cantidad;

    private Double latitud;
    private Double longitud;

    @Column(nullable = false, length = 80)
    private String departamento;

    @Column(length = 80)
    private String region;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected Avistamiento() { }

    public Avistamiento(String investigadorId, Especie especie, Instant fechaHora,
                        int cantidad, Double latitud, Double longitud,
                        String departamento, String region) {
        this.id = UUID.randomUUID();
        this.creadoEn = Instant.now();
        this.investigadorId = investigadorId;
        this.especie = especie;
        this.fechaHora = fechaHora;
        this.cantidad = cantidad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.departamento = departamento;
        this.region = region;
    }

    public UUID getId() {
        return id;
    }

    public String getInvestigadorId() {
        return investigadorId;
    }

    public Especie getEspecie() {
        return especie;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public int getCantidad() {
        return cantidad;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getRegion() {
        return region;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    
}