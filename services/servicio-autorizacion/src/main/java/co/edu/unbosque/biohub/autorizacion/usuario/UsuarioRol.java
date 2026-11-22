package co.edu.unbosque.biohub.autorizacion.usuario;

import co.edu.unbosque.biohub.autorizacion.rol.Rol;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario_roles", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "rol_id"}))
public class UsuarioRol {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    protected UsuarioRol() {}
    public UsuarioRol(String usuarioId, Rol rol) {
        this.usuarioId = usuarioId;
        this.rol = rol;
    }
    public Long getId() { return id; }
    public String getUsuarioId() { return usuarioId; }
    public Rol getRol() { return rol; }
}
