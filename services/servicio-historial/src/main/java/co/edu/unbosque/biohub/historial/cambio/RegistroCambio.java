package co.edu.unbosque.biohub.historial.cambio;

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

@Entity
@Immutable
@Table(name = "registro_cambio")
public class RegistroCambio {

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
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cambio", nullable = false, length = 20)
    private TipoCambio tipoCambio;
    @Column(length = 100)
    private String campo;
    @Column(name = "valor_anterior", length = 1000)
    private String valorAnterior;
    @Column(name = "valor_nuevo", length = 1000)
    private String valorNuevo;
    @Column(length = 500)
    private String motivo;
    @Column(nullable = false, length = 100)
    private String autor;
    @Column(length = 100)
    private String autorizador;
    @Column(name = "ocurrio_en", nullable = false)
    private Instant ocurrioEn;
    @Column(name = "registrado_en", nullable = false)
    private Instant registradoEn;
    @Column(name = "carga_util", nullable = false, columnDefinition = "TEXT")
    private String cargaUtil;
    @Column(name = "hash_anterior", length = 64)
    private String hashAnterior;
    @Column(nullable = false, length = 64)
    private String hash;

    protected RegistroCambio() {}

    RegistroCambio(String idEvento, TipoRegistro tipoRegistro, String idRegistro, int version, TipoCambio tipoCambio,
                   String campo, String valorAnterior, String valorNuevo, String motivo, String autor,
                   String autorizador, Instant ocurrioEn, String cargaUtil, String hashAnterior, String hash) {
        this.idEvento = idEvento;
        this.tipoRegistro = tipoRegistro;
        this.idRegistro = idRegistro;
        this.version = version;
        this.tipoCambio = tipoCambio;
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.motivo = motivo;
        this.autor = autor;
        this.autorizador = autorizador;
        this.ocurrioEn = ocurrioEn;
        this.registradoEn = Instant.now();
        this.cargaUtil = cargaUtil;
        this.hashAnterior = hashAnterior;
        this.hash = hash;
    }

    public String describir() {
        String quien = autorizador == null ? autor : autor + " (autorizado por " + autorizador + ")";
        return switch (tipoCambio) {
            case CREACION -> "Versión " + version + ": registro creado por " + quien;
            case MODIFICACION -> "Versión " + version + ": " + campo + " cambió de '" + texto(valorAnterior)
                    + "' a '" + texto(valorNuevo) + "' por " + quien;
            case RECLASIFICACION -> "Versión " + version + ": reclasificado de '" + texto(valorAnterior)
                    + "' a '" + texto(valorNuevo) + "' por " + quien + (motivo == null ? "" : ". Motivo: " + motivo);
            case DESACTIVACION -> "Versión " + version + ": desactivado por " + quien
                    + (motivo == null ? "" : ". Motivo: " + motivo);
        };
    }

    private static String texto(String valor) {
        return valor == null ? "(vacío)" : valor;
    }

    public Long getId() { return id; }
    public String getIdEvento() { return idEvento; }
    public TipoRegistro getTipoRegistro() { return tipoRegistro; }
    public String getIdRegistro() { return idRegistro; }
    public int getVersion() { return version; }
    public TipoCambio getTipoCambio() { return tipoCambio; }
    public String getCampo() { return campo; }
    public String getValorAnterior() { return valorAnterior; }
    public String getValorNuevo() { return valorNuevo; }
    public String getMotivo() { return motivo; }
    public String getAutor() { return autor; }
    public String getAutorizador() { return autorizador; }
    public Instant getOcurrioEn() { return ocurrioEn; }
    public Instant getRegistradoEn() { return registradoEn; }
    public String getCargaUtil() { return cargaUtil; }
    public String getHashAnterior() { return hashAnterior; }
    public String getHash() { return hash; }
}
