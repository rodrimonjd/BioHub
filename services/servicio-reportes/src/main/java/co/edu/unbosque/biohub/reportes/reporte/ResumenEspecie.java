package co.edu.unbosque.biohub.reportes.reporte;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Cuantos avistamientos e individuos tuvo una especie dentro del reporte. */
@Entity
@Table(name = "resumen_especie")
public class ResumenEspecie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporte_id")
    @JsonIgnore
    private Reporte reporte;

    @Column(name = "especie_id", nullable = false, length = 36)
    private String especieId;

    @Column(length = 150)
    private String especie;

    @Column(nullable = false)
    private int avistamientos;

    @Column(nullable = false)
    private int individuos;

    protected ResumenEspecie() { }

    ResumenEspecie(Reporte reporte, String especieId, String especie) {
        this.reporte = reporte;
        this.especieId = especieId;
        this.especie = especie;
    }

    void sumar(int cantidad) {
        avistamientos++;
        individuos += cantidad;
    }

    public String getEspecieId() { return especieId; }
    public String getEspecie() { return especie; }
    public int getAvistamientos() { return avistamientos; }
    public int getIndividuos() { return individuos; }
}
