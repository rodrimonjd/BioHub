package co.edu.unbosque.biohub.registro_service.expedicion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unbosque.biohub.registro_service.equipo.EquipoCampo;
import co.edu.unbosque.biohub.registro_service.equipo.EstadoEquipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "expedicion")
public class Expedicion {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String zona;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "expedicion_equipo",
            joinColumns = @JoinColumn(name = "expedicion_id"),
            inverseJoinColumns = @JoinColumn(name = "equipo_id"))
    private Set<EquipoCampo> equipos = new HashSet<>();

    protected Expedicion() { }

    public Expedicion(String nombre, String zona, LocalDate fechaInicio, LocalDate fechaFin) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre es obligatorio");
        }
        if (zona == null || zona.isBlank()) {
            throw new IllegalArgumentException("zona es obligatoria");
        }
        if (fechaInicio == null) {
            throw new IllegalArgumentException("fechaInicio es obligatoria");
        }
        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("fechaFin no puede ser anterior a fechaInicio");
        }
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.zona = zona;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.creadoEn = Instant.now();
    }

    public void asignarEquipo(EquipoCampo equipo) {
        if (equipo.getEstado() == EstadoEquipo.MANTENIMIENTO) {
            throw new IllegalArgumentException(
                    "El equipo " + equipo.getNumeroSerie() + " esta en mantenimiento");
        }
        equipos.add(equipo);
    }

    public List<EquipoCampo> listarEquipos() {
        return new ArrayList<>(equipos);
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getZona() { return zona; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public Instant getCreadoEn() { return creadoEn; }
}