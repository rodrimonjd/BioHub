package co.edu.unbosque.biohub.registro_service.evidencia;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.edu.unbosque.biohub.registro_service.hallazgo.Hallazgo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "evidencia")
public class Evidencia {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hallazgo_id")
    private Hallazgo hallazgo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoEvidencia tipo;

    @Column(length = 500)
    private String valor;

    @Column(length = 20)
    private String unidad;

    @Column(name = "url_archivo", length = 500)
    private String urlArchivo;

    @Column(name = "atributo_taxonomico", length = 100)
    private String atributoTaxonomico;

    protected Evidencia() { }

    Evidencia(TipoEvidencia tipo, String valor, String unidad,
              String urlArchivo, String atributoTaxonomico) {
        this.id = UUID.randomUUID();
        this.tipo = tipo;
        this.valor = valor;
        this.unidad = unidad;
        this.urlArchivo = urlArchivo;
        this.atributoTaxonomico = atributoTaxonomico;
    }

    /**
     * Revisa que la evidencia tenga los datos que exige su tipo:
     * TEMPERATURA un valor numerico y una unidad, ADN el codigo de la muestra
     * y AUDIO la URL del archivo.
     */
    public boolean validarFormato() {
        if (tipo == null) {
            return false;
        }
        switch (tipo) {
            case TEMPERATURA -> {
                if (esVacio(valor) || esVacio(unidad)) {
                    return false;
                }
                try {
                    Double.parseDouble(valor.trim());
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
            }
            case ADN -> {
                return !esVacio(valor);
            }
            case AUDIO -> {
                return !esVacio(urlArchivo);
            }
        }
        return false;
    }

    private static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    public void asignarHallazgo(Hallazgo hallazgo) {
        this.hallazgo = hallazgo;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Hallazgo getHallazgo() {
        return hallazgo;
    }

    public void setHallazgo(Hallazgo hallazgo) {
        this.hallazgo = hallazgo;
    }

    public TipoEvidencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvidencia tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getUrlArchivo() {
        return urlArchivo;
    }

    public void setUrlArchivo(String urlArchivo) {
        this.urlArchivo = urlArchivo;
    }

    public String getAtributoTaxonomico() {
        return atributoTaxonomico;
    }

    public void setAtributoTaxonomico(String atributoTaxonomico) {
        this.atributoTaxonomico = atributoTaxonomico;
    }

    
}