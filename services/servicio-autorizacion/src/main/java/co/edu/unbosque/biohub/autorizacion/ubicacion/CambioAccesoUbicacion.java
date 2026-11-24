package co.edu.unbosque.biohub.autorizacion.ubicacion;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "historial_acceso_ubicacion")
public class CambioAccesoUbicacion {

    public enum Accion { OTORGADO, REVOCADO }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "usuario_id", nullable = false, length = 100, updatable = false)
    private String usuarioId;
    @Column(name = "especie_id", nullable = false, length = 36, updatable = false)
    private String especieId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private Accion accion;
    @Column(name = "realizado_por", nullable = false, length = 100, updatable = false)
    private String realizadoPor;
    @Column(name = "realizado_en", nullable = false, updatable = false)
    private Instant realizadoEn;

    protected CambioAccesoUbicacion() {}

    public CambioAccesoUbicacion(String usuarioId, String especieId, Accion accion, String realizadoPor) {
        this.usuarioId = usuarioId;
        this.especieId = especieId;
        this.accion = accion;
        this.realizadoPor = realizadoPor;
        this.realizadoEn = Instant.now();
    }

    public Long getId() { return id; }
    public String getUsuarioId() { return usuarioId; }
    public String getEspecieId() { return especieId; }
    public Accion getAccion() { return accion; }
    public String getRealizadoPor() { return realizadoPor; }
    public Instant getRealizadoEn() { return realizadoEn; }
}
