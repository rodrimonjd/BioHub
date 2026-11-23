package co.edu.unbosque.biohub.reportes.reporte;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "indice_presion")
public class IndicePresion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporte_id")
    @JsonIgnore
    private Reporte reporte;

    @Column(nullable = false, length = 170)
    private String ecosistema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoIndice tipo;

    @Column(nullable = false)
    private double valor;

    @Column(nullable = false)
    private double umbral;

    @Column(name = "supera_umbral", nullable = false)
    private boolean superaUmbral;

    @Column(nullable = false)
    private int avistamientos;

    @Column(nullable = false)
    private int individuos;

    @Column(name = "fecha_calculo", nullable = false)
    private Instant fechaCalculo;

    protected IndicePresion() { }

    IndicePresion(Reporte reporte, String ecosistema, TipoIndice tipo, double valor, int avistamientos, int individuos) {
        this.reporte = reporte;
        this.ecosistema = ecosistema;
        this.tipo = tipo;
        this.valor = Math.round(valor * 100.0) / 100.0;
        this.umbral = tipo.getUmbralPorDefecto();
        this.superaUmbral = superaUmbral(umbral);
        this.avistamientos = avistamientos;
        this.individuos = individuos;
        this.fechaCalculo = Instant.now();
    }

    public boolean superaUmbral(double umbral) {
        return valor > umbral;
    }

    public String getEcosistema() { return ecosistema; }
    public TipoIndice getTipo() { return tipo; }
    public double getValor() { return valor; }
    public double getUmbral() { return umbral; }
    public boolean isSuperaUmbral() { return superaUmbral; }
    public int getAvistamientos() { return avistamientos; }
    public int getIndividuos() { return individuos; }
    public Instant getFechaCalculo() { return fechaCalculo; }
}
