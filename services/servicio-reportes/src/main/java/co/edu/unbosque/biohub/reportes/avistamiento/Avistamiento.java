package co.edu.unbosque.biohub.reportes.avistamiento;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Copia de un avistamiento de servicio-registro. Se llena con el evento
 * AVISTAMIENTO_REGISTRADO; reportes nunca consulta la base de registro.
 */
@Entity
@Table(name = "avistamiento")
public class Avistamiento {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "especie_id", nullable = false, length = 36)
    private String especieId;

    @Column(length = 150)
    private String especie;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, length = 80)
    private String departamento;

    @Column(length = 80)
    private String region;

    @Column(name = "recibido_en", nullable = false)
    private Instant recibidoEn;

    protected Avistamiento() { }

    public Avistamiento(String id, String especieId, String especie, Instant fechaHora,
                        int cantidad, String departamento, String region) {
        this.id = id;
        this.especieId = especieId;
        this.especie = especie;
        this.fechaHora = fechaHora;
        this.cantidad = cantidad;
        this.departamento = departamento;
        this.region = region;
        this.recibidoEn = Instant.now();
    }

    /** Ecosistema al que se le calcula el indice: "departamento - region", o solo el departamento. */
    public String ecosistema() {
        if (region == null || region.isBlank()) {
            return departamento;
        }
        return departamento + " - " + region;
    }

    public String getId() { return id; }
    public String getEspecieId() { return especieId; }
    public String getEspecie() { return especie; }
    public Instant getFechaHora() { return fechaHora; }
    public int getCantidad() { return cantidad; }
    public String getDepartamento() { return departamento; }
    public String getRegion() { return region; }
}
