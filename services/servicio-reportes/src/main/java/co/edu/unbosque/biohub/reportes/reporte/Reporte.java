package co.edu.unbosque.biohub.reportes.reporte;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import co.edu.unbosque.biohub.reportes.avistamiento.Avistamiento;
import co.edu.unbosque.biohub.reportes.compartido.DatosInvalidosException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Reporte completo de un periodo: para cada ecosistema calcula todos los indices de presion
 * (uno por cada TipoIndice) y resume los avistamientos por especie.
 */
@Entity
@Table(name = "reporte")
public class Reporte {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(length = 80)
    private String departamento;

    @Column(name = "especie_id", length = 36)
    private String especieId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReporte estado;

    @Column(name = "solicitado_por", nullable = false, length = 100)
    private String solicitadoPor;

    @Column(length = 500)
    private String mensaje;

    @Column(name = "total_avistamientos", nullable = false)
    private int totalAvistamientos;

    @Column(name = "total_individuos", nullable = false)
    private int totalIndividuos;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "generado_en")
    private Instant generadoEn;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("ecosistema ASC, tipo ASC")
    private List<IndicePresion> indices = new ArrayList<>();

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("individuos DESC")
    private List<ResumenEspecie> especies = new ArrayList<>();

    protected Reporte() { }

    public Reporte(LocalDate fechaInicio, LocalDate fechaFin, String departamento, String especieId, String solicitadoPor) {
        if (fechaInicio == null || fechaFin == null) {
            throw new DatosInvalidosException("El periodo (fechaInicio y fechaFin) es obligatorio");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new DatosInvalidosException("fechaFin no puede ser anterior a fechaInicio");
        }
        if (solicitadoPor == null || solicitadoPor.isBlank()) {
            throw new DatosInvalidosException("Quien solicita el reporte es obligatorio");
        }
        this.id = UUID.randomUUID().toString();
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.departamento = (departamento == null || departamento.isBlank()) ? null : departamento.trim();
        this.especieId = (especieId == null || especieId.isBlank()) ? null : especieId.trim();
        this.solicitadoPor = solicitadoPor.trim();
        this.estado = EstadoReporte.EN_COLA;
        this.creadoEn = Instant.now();
    }

    public void iniciarGeneracion() {
        this.estado = EstadoReporte.GENERANDO;
    }

    public void generar(List<Avistamiento> avistamientosDelPeriodo) {
        totalAvistamientos = 0;
        totalIndividuos = 0;
        indices.clear();
        especies.clear();

        Map<String, List<Avistamiento>> porEcosistema = new TreeMap<>();
        Map<String, ResumenEspecie> porEspecie = new LinkedHashMap<>();
        for (Avistamiento a : avistamientosDelPeriodo) {
            if (departamento != null && !departamento.equalsIgnoreCase(a.getDepartamento())) {
                continue;
            }
            if (especieId != null && !especieId.equals(a.getEspecieId())) {
                continue;
            }
            porEcosistema.computeIfAbsent(a.ecosistema(), k -> new ArrayList<>()).add(a);
            porEspecie.computeIfAbsent(a.getEspecieId(), k -> new ResumenEspecie(this, a.getEspecieId(), a.getEspecie()))
                    .sumar(a.getCantidad());
            totalAvistamientos++;
            totalIndividuos += a.getCantidad();
        }

        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        for (Map.Entry<String, List<Avistamiento>> grupo : porEcosistema.entrySet()) {
            int individuos = 0;
            for (Avistamiento a : grupo.getValue()) {
                individuos += a.getCantidad();
            }
            for (TipoIndice tipo : TipoIndice.values()) {
                double valor = tipo.calcular(grupo.getValue(), dias);
                indices.add(new IndicePresion(this, grupo.getKey(), tipo, valor, grupo.getValue().size(), individuos));
            }
        }
        especies.addAll(porEspecie.values());
        especies.sort((a, b) -> Integer.compare(b.getIndividuos(), a.getIndividuos()));

        this.generadoEn = Instant.now();
        if (totalAvistamientos == 0) {
            this.estado = EstadoReporte.SIN_DATOS;
            this.mensaje = "No hay avistamientos para los parametros seleccionados";
        } else {
            this.estado = EstadoReporte.LISTO;
            this.mensaje = null;
        }
    }

    public void marcarError(String motivo) {
        this.estado = EstadoReporte.ERROR;
        this.mensaje = motivo;
        this.generadoEn = Instant.now();
    }

    /** Ecosistemas distintos del reporte, en el orden de la tabla de indices. */
    public List<String> ecosistemas() {
        List<String> nombres = new ArrayList<>();
        for (IndicePresion i : indices) {
            if (!nombres.contains(i.getEcosistema())) {
                nombres.add(i.getEcosistema());
            }
        }
        return nombres;
    }

    public IndicePresion indice(String ecosistema, TipoIndice tipo) {
        for (IndicePresion i : indices) {
            if (i.getEcosistema().equals(ecosistema) && i.getTipo() == tipo) {
                return i;
            }
        }
        return null;
    }

    public String getId() { return id; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getDepartamento() { return departamento; }
    public String getEspecieId() { return especieId; }
    public EstadoReporte getEstado() { return estado; }
    public String getSolicitadoPor() { return solicitadoPor; }
    public String getMensaje() { return mensaje; }
    public int getTotalAvistamientos() { return totalAvistamientos; }
    public int getTotalIndividuos() { return totalIndividuos; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getGeneradoEn() { return generadoEn; }
    public List<IndicePresion> getIndices() { return indices; }
    public List<ResumenEspecie> getEspecies() { return especies; }
}
