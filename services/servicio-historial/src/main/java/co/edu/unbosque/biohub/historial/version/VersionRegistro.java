package co.edu.unbosque.biohub.historial.version;

import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

/**
 * Estado completo de un registro científico en una versión ("qué se sabía en esa fecha").
 * Inmutable: cada cambio crea una versión nueva y las anteriores nunca se sobrescriben (ASR-07).
 */
@Entity
@Immutable
@Table(name = "version_registro")
public class VersionRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_evento", nullable = false, length = 36)
    private String idEvento;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_registro", nullable = false, length = 20)
    private TipoRegistro tipoRegistro;
    @Column(name = "id_registro", nullable = false, length = 36)
    private String idRegistro;
    @Column(nullable = false)
    private int version;
    /** JSON con todos los campos; null si llegó un cambio sin conocer la versión anterior. */
    @Column(columnDefinition = "TEXT")
    private String estado;
    @Column(nullable = false, length = 100)
    private String autor;
    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    protected VersionRegistro() {}

    public VersionRegistro(String idEvento, TipoRegistro tipoRegistro, String idRegistro, int version,
                           String estado, String autor, Instant creadaEn) {
        this.idEvento = idEvento;
        this.tipoRegistro = tipoRegistro;
        this.idRegistro = idRegistro;
        this.version = version;
        this.estado = estado;
        this.autor = autor;
        this.creadaEn = creadaEn;
    }

    public Long getId() { return id; }
    public String getIdEvento() { return idEvento; }
    public TipoRegistro getTipoRegistro() { return tipoRegistro; }
    public String getIdRegistro() { return idRegistro; }
    public int getVersion() { return version; }
    public String getEstado() { return estado; }
    public String getAutor() { return autor; }
    public Instant getCreadaEn() { return creadaEn; }
}
