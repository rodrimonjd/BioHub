package co.edu.unbosque.biohub.registro_service.evento;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_evento")
public class OutboxEvento {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "id_agregado", nullable = false, length = 36)
    private String idAgregado;

    @Column(name = "ocurrio_en", nullable = false)
    private Instant ocurrioEn;

    @Column(nullable = false, length = 4000)
    private String payload;

    @Column(nullable = false)
    private boolean publicado;

    @Column(name = "publicado_en")
    private Instant publicadoEn;

    @Column(nullable = false)
    private int intentos;

    protected OutboxEvento() { }

    public OutboxEvento(UUID id, String tipo, String idAgregado,
                        Instant ocurrioEn, String payload) {
        this.id = id;
        this.tipo = tipo;
        this.idAgregado = idAgregado;
        this.ocurrioEn = ocurrioEn;
        this.payload = payload;
        this.publicado = false;
        this.intentos = 0;
    }

    public void marcarPublicado() {
        this.publicado = true;
        this.publicadoEn = Instant.now();
    }

    public void registrarFallo() {
        this.intentos++;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIdAgregado() {
        return idAgregado;
    }

    public void setIdAgregado(String idAgregado) {
        this.idAgregado = idAgregado;
    }

    public Instant getOcurrioEn() {
        return ocurrioEn;
    }

    public void setOcurrioEn(Instant ocurrioEn) {
        this.ocurrioEn = ocurrioEn;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isPublicado() {
        return publicado;
    }

    public void setPublicado(boolean publicado) {
        this.publicado = publicado;
    }

    public Instant getPublicadoEn() {
        return publicadoEn;
    }

    public void setPublicadoEn(Instant publicadoEn) {
        this.publicadoEn = publicadoEn;
    }

    public int getIntentos() {
        return intentos;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

}