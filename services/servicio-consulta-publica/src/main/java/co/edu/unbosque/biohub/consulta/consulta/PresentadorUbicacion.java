package co.edu.unbosque.biohub.consulta.consulta;

import co.edu.unbosque.biohub.consulta.especie.EspecieInfo;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLectura;
import co.edu.unbosque.biohub.consulta.visibilidad.NivelVisibilidad;

public final class PresentadorUbicacion {

    private PresentadorUbicacion() {}

    public static VistaHallazgoPublico presentar(HallazgoLectura h, EspecieInfo especie, NivelVisibilidad nivel) {
        boolean exacta = nivel == NivelVisibilidad.EXACTA;
        boolean conRegion = exacta || nivel == NivelVisibilidad.REGION;
        return new VistaHallazgoPublico(
                h.getId(),
                h.getEspecieId(),
                h.getEspecie(),
                especie == null ? null : especie.nombreComun(),
                especie == null ? null : especie.estadoConservacion(),
                h.getFecha(),
                h.getDescripcion(),
                h.getDepartamento(),
                conRegion ? h.getRegion() : null,
                exacta ? h.getLatitud() : null,
                exacta ? h.getLongitud() : null,
                nivel,
                h.getVersion());
    }
}
