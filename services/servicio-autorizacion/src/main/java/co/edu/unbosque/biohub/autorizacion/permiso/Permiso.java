package co.edu.unbosque.biohub.autorizacion.permiso;

import jakarta.persistence.*;

@Entity
@Table(name = "permisos")
public class Permiso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String codigo;
    private String descripcion;

    protected Permiso() {}
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
}
