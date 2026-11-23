package co.edu.unbosque.biohub.autorizacion.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Cuenta de un usuario del panel. El id es el mismo usuario_id de {@link UsuarioRol}.
 * Solo guarda el hash BCrypt de la contraseña.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(length = 100)
    private String id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(nullable = false, unique = true, length = 150)
    private String correo;
    @Column(name = "hash_clave", nullable = false, length = 100)
    private String hashClave;
    @Column(nullable = false)
    private boolean activo = true;
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    protected Usuario() {}

    public Usuario(String id, String nombre, String correo, String hashClave) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.hashClave = hashClave;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getHashClave() { return hashClave; }
    public boolean isActivo() { return activo; }
    public Instant getCreadoEn() { return creadoEn; }
}
