package co.edu.unbosque.biohub.registro_service.especie;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Table(name = "especie")
public class Especie {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Column(name = "nombre_cientifico", nullable = false, length = 150)
    private String nombreCientifico;

    @Column(name = "nombre_comun", length = 150)
    private String nombreComun;

    @Column(name = "estado_conservacion", nullable = false, length = 30)
    private String estadoConservacion;

    protected Especie() { }   // lo exige JPA

    public Especie(String nombreCientifico, String nombreComun, String estadoConservacion) {
        this.id = UUID.randomUUID();
        this.nombreCientifico = nombreCientifico;
        this.nombreComun = nombreComun;
        this.estadoConservacion = estadoConservacion;
    }

    public boolean esSensible() {
        return "CR".equals(estadoConservacion) || "EN".equals(estadoConservacion);
    }

    public UUID getId() {
        return id;
    }

    public String getNombreCientifico() {
        return nombreCientifico;
    }

    public String getNombreComun() {
        return nombreComun;
    }

    public String getEstadoConservacion() {
        return estadoConservacion;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setNombreCientifico(String nombreCientifico) {
        this.nombreCientifico = nombreCientifico;
    }

    public void setNombreComun(String nombreComun) {
        this.nombreComun = nombreComun;
    }

    public void setEstadoConservacion(String estadoConservacion) {
        this.estadoConservacion = estadoConservacion;
    }

    
}