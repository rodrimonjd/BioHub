package co.edu.unbosque.biohub.registro_service.equipo;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "equipo_campo")
public class EquipoCampo {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 50)
    private String numeroSerie;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(length = 100)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private EstadoEquipo estado;

    @Column(name = "responsable_id", length = 36)
    private String responsableId;

    protected EquipoCampo() { }

    public EquipoCampo(String numeroSerie, String tipo, String modelo) {
        if (numeroSerie == null || numeroSerie.isBlank()) {
            throw new IllegalArgumentException("numeroSerie es obligatorio");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("tipo es obligatorio");
        }
        this.id = UUID.randomUUID();
        this.numeroSerie = numeroSerie;
        this.tipo = tipo;
        this.modelo = modelo;
        this.estado = EstadoEquipo.DISPONIBLE;
    }

    public void asignarResponsable(String investigadorId) {
        if (investigadorId == null || investigadorId.isBlank()) {
            throw new IllegalArgumentException("investigadorId es obligatorio");
        }
        this.responsableId = investigadorId;
    }

    public void cambiarEstado(EstadoEquipo nuevo) {
        this.estado = nuevo;
    }

    public UUID getId() { return id; }
    public String getNumeroSerie() { return numeroSerie; }
    public String getTipo() { return tipo; }
    public String getModelo() { return modelo; }
    public EstadoEquipo getEstado() { return estado; }
    public String getResponsableId() { return responsableId; }
}