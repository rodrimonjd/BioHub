package co.edu.unbosque.biohub.reportes.reporte;

import java.util.ArrayList;
import java.util.List;

import co.edu.unbosque.biohub.reportes.avistamiento.Avistamiento;

public enum TipoIndice {

    DENSIDAD_OBSERVACION(10.0, "Densidad de observación", "ind/día",
            "Individuos observados en el ecosistema dividido entre los días del periodo.") {
        @Override
        public double calcular(List<Avistamiento> avistamientos, long dias) {
            int individuos = 0;
            for (Avistamiento a : avistamientos) {
                individuos += a.getCantidad();
            }
            return (double) individuos / dias;
        }
    },

    RIQUEZA_ESPECIES(5.0, "Riqueza de especies", "especies",
            "Número de especies distintas observadas en el ecosistema durante el periodo.") {
        @Override
        public double calcular(List<Avistamiento> avistamientos, long dias) {
            List<String> especies = new ArrayList<>();
            for (Avistamiento a : avistamientos) {
                if (!especies.contains(a.getEspecieId())) {
                    especies.add(a.getEspecieId());
                }
            }
            return especies.size();
        }
    };

    private final double umbralPorDefecto;
    private final String nombre;
    private final String unidad;
    private final String descripcion;

    TipoIndice(double umbralPorDefecto, String nombre, String unidad, String descripcion) {
        this.umbralPorDefecto = umbralPorDefecto;
        this.nombre = nombre;
        this.unidad = unidad;
        this.descripcion = descripcion;
    }

    public abstract double calcular(List<Avistamiento> avistamientos, long dias);

    public double getUmbralPorDefecto() { return umbralPorDefecto; }
    public String getNombre() { return nombre; }
    public String getUnidad() { return unidad; }
    public String getDescripcion() { return descripcion; }
}
