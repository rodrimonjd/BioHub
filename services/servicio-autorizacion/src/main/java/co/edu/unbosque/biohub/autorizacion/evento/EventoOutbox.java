package co.edu.unbosque.biohub.autorizacion.evento;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evento_outbox")
public class EventoOutbox {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "id_agregado", nullable = false)
    private String idAgregado;
    @Column(nullable = false)
    private String tipo;
    @Column(name = "carga_util", nullable = false, columnDefinition = "TEXT")
    private String cargaUtil;
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();
    private boolean publicado = false;

    protected EventoOutbox() {}
    public EventoOutbox(String idAgregado, String tipo, String cargaUtil) {
        this.idAgregado = idAgregado;
        this.tipo = tipo;
        this.cargaUtil = cargaUtil;
    }
    public Long getId() { return id; }
    public String getIdAgregado() { return idAgregado; }
    public String getTipo() { return tipo; }
    public String getCargaUtil() { return cargaUtil; }
    public Instant getCreadoEn() { return creadoEn; }
    public boolean isPublicado() { return publicado; }
    public void marcarPublicado() { this.publicado = true; }
}
