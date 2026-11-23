package co.edu.unbosque.biohub.historial.cambio;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ConstructorRegistroCambio {

    private String idEvento;
    private TipoRegistro tipoRegistro;
    private String idRegistro;
    private Integer version;
    private TipoCambio tipoCambio;
    private String campo;
    private String valorAnterior;
    private String valorNuevo;
    private String motivo;
    private String autor;
    private String autorizador;
    private Instant ocurrioEn;
    private String cargaUtil;
    private String hashAnterior;
    private String estadoResultante;

    public ConstructorRegistroCambio evento(String idEvento, String cargaUtil) {
        this.idEvento = idEvento;
        this.cargaUtil = cargaUtil;
        return this;
    }

    public ConstructorRegistroCambio registro(TipoRegistro tipo, String id, Integer version) {
        this.tipoRegistro = tipo;
        this.idRegistro = id;
        this.version = version;
        return this;
    }

    public ConstructorRegistroCambio tipo(TipoCambio tipoCambio) {
        this.tipoCambio = tipoCambio;
        return this;
    }

    public ConstructorRegistroCambio valores(String campo, String valorAnterior, String valorNuevo) {
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        return this;
    }

    public ConstructorRegistroCambio motivo(String motivo) {
        this.motivo = motivo;
        return this;
    }

    public ConstructorRegistroCambio responsables(String autor, String autorizador) {
        this.autor = autor;
        this.autorizador = autorizador;
        return this;
    }

    public ConstructorRegistroCambio ocurrioEn(Instant ocurrioEn) {
        this.ocurrioEn = ocurrioEn;
        return this;
    }

    /** Hash del cambio anterior del mismo registro (null si es el primero). */
    public ConstructorRegistroCambio encadenarCon(String hashAnterior) {
        this.hashAnterior = hashAnterior;
        return this;
    }

    /** Estado del registro después del cambio (JSON); entra en el hash para proteger también la versión. */
    public ConstructorRegistroCambio estadoResultante(String estadoJson) {
        this.estadoResultante = estadoJson;
        return this;
    }

    public RegistroCambio construir() {
        exigir(idEvento, "el id del evento");
        exigir(cargaUtil, "el evento original");
        exigir(tipoRegistro, "el tipo de registro");
        exigir(idRegistro, "el id del registro");
        exigir(tipoCambio, "el tipo de cambio");
        exigir(autor, "el autor");
        exigir(ocurrioEn, "la fecha del cambio");
        if (version == null || version < 1) {
            throw new RegistroCambioIncompletoException("La versión debe ser 1 o mayor");
        }
        switch (tipoCambio) {
            case MODIFICACION -> {
                exigir(campo, "el campo modificado");
                exigir(valorNuevo, "el valor nuevo");
            }
            case RECLASIFICACION -> {
                exigir(valorAnterior, "la especie anterior");
                exigir(valorNuevo, "la especie nueva");
                exigir(autorizador, "el autorizador de la reclasificación");
                exigir(motivo, "el motivo de la reclasificación");
            }
            case CREACION, DESACTIVACION -> { }
        }
        // la base de datos guarda microsegundos: se recorta aquí para que el hash se pueda recalcular igual
        Instant fecha = ocurrioEn.truncatedTo(ChronoUnit.MICROS);
        String hash = calcularHash(hashAnterior, idEvento, tipoRegistro, idRegistro, version, tipoCambio, campo,
                valorAnterior, valorNuevo, motivo, autor, autorizador, fecha, estadoResultante);
        return new RegistroCambio(idEvento, tipoRegistro, idRegistro, version, tipoCambio, campo, valorAnterior,
                valorNuevo, motivo, autor, autorizador, fecha, cargaUtil, hashAnterior, hash);
    }

    /** Misma fórmula que usa la verificación de integridad. */
    public static String calcularHash(RegistroCambio c, String estadoResultante) {
        return calcularHash(c.getHashAnterior(), c.getIdEvento(), c.getTipoRegistro(), c.getIdRegistro(),
                c.getVersion(), c.getTipoCambio(), c.getCampo(), c.getValorAnterior(), c.getValorNuevo(),
                c.getMotivo(), c.getAutor(), c.getAutorizador(), c.getOcurrioEn(), estadoResultante);
    }

    private static String calcularHash(String hashAnterior, String idEvento, TipoRegistro tipoRegistro,
                                       String idRegistro, int version, TipoCambio tipoCambio, String campo,
                                       String valorAnterior, String valorNuevo, String motivo, String autor,
                                       String autorizador, Instant ocurrioEn, String estado) {
        return CalculadoraHash.calcular(hashAnterior, idEvento, tipoRegistro, idRegistro, version, tipoCambio,
                campo, valorAnterior, valorNuevo, motivo, autor, autorizador, ocurrioEn, estado);
    }

    private static void exigir(Object valor, String nombre) {
        if (valor == null || (valor instanceof String s && s.isBlank())) {
            throw new RegistroCambioIncompletoException("Falta " + nombre);
        }
    }
}
