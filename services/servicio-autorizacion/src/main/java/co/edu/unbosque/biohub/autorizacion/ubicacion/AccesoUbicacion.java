package co.edu.unbosque.biohub.autorizacion.ubicacion;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "acceso_ubicacion", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "especie_id"}))
public class AccesoUbicacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;
    @Column(name = "especie_id", nullable = false, length = 36)
    private String especieId;
    @Column(name = "otorgado_por", nullable = false, length = 100)
    private String otorgadoPor;
    @Column(name = "otorgado_en", nullable = false, updatable = false)
    private Instant otorgadoEn;

    protected AccesoUbicacion() {}

    public AccesoUbicacion(String usuarioId, String especieId, String otorgadoPor) {
        this.usuarioId = usuarioId;
        this.especieId = especieId;
        this.otorgadoPor = otorgadoPor;
        this.otorgadoEn = Instant.now();
    }

    public Long getId() { return id; }
    public String getUsuarioId() { return usuarioId; }
    public String getEspecieId() { return especieId; }
    public String getOtorgadoPor() { return otorgadoPor; }
    public Instant getOtorgadoEn() { return otorgadoEn; }
}
