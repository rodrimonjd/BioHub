package co.edu.unbosque.biohub.autorizacion.rol;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "historial_roles")
public class CambioRol {

    public enum Accion { ROL_ASIGNADO, ROL_QUITADO, ROL_CREADO, PERMISOS_CAMBIADOS }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private Accion accion;
    @Column(name = "usuario_id", length = 100, updatable = false)
    private String usuarioId;
    @Column(nullable = false, length = 60, updatable = false)
    private String rol;
    @Column(length = 2000, updatable = false)
    private String detalle;
    @Column(name = "realizado_por", nullable = false, length = 100, updatable = false)
    private String realizadoPor;
    @Column(name = "realizado_en", nullable = false, updatable = false)
    private Instant realizadoEn;

    protected CambioRol() {}

    public CambioRol(Accion accion, String usuarioId, String rol, String detalle, String realizadoPor) {
        this.accion = accion;
        this.usuarioId = usuarioId;
        this.rol = rol;
        this.detalle = detalle;
        this.realizadoPor = realizadoPor;
        this.realizadoEn = Instant.now();
    }

    public Long getId() { return id; }
    public Accion getAccion() { return accion; }
    public String getUsuarioId() { return usuarioId; }
    public String getRol() { return rol; }
    public String getDetalle() { return detalle; }
    public String getRealizadoPor() { return realizadoPor; }
    public Instant getRealizadoEn() { return realizadoEn; }
}
