package co.edu.unbosque.biohub.consulta.consulta;

import co.edu.unbosque.biohub.consulta.compartido.NoEncontradoException;
import co.edu.unbosque.biohub.consulta.especie.CatalogoEspecies;
import co.edu.unbosque.biohub.consulta.especie.EspecieInfo;
import co.edu.unbosque.biohub.consulta.lectura.BusquedaHallazgos;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLectura;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLecturaRepositorio;
import co.edu.unbosque.biohub.consulta.visibilidad.ClienteAutorizacion;
import co.edu.unbosque.biohub.consulta.visibilidad.NivelVisibilidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ServicioConsulta {

    public static final int TAMANO_MAXIMO = 100;

    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    private final HallazgoLecturaRepositorio repositorio;
    private final CatalogoEspecies catalogo;
    private final ClienteAutorizacion autorizacion;

    public ServicioConsulta(HallazgoLecturaRepositorio repositorio, CatalogoEspecies catalogo,
                            ClienteAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.catalogo = catalogo;
        this.autorizacion = autorizacion;
    }

    public PaginaHallazgos listar(String usuarioId, FiltroHallazgos filtro, int pagina, int tamano) {
        if (pagina < 0) throw new IllegalArgumentException("pagina debe ser 0 o mayor");
        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("tamano debe estar entre 1 y " + TAMANO_MAXIMO);
        }
        filtro.validar();
        Page<HallazgoLectura> resultado = repositorio.findAll(busqueda(filtro).especificacion(),
                PageRequest.of(pagina, tamano, BusquedaHallazgos.ORDEN));
        List<VistaHallazgoPublico> contenido = resultado.getContent().stream()
                .map(h -> presentar(usuarioId, h))
                .toList();
        return new PaginaHallazgos(contenido, pagina, tamano, resultado.getTotalElements(), resultado.getTotalPages());
    }

    public VistaHallazgoPublico obtener(String usuarioId, String id) {
        HallazgoLectura h = repositorio.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Hallazgo no encontrado: " + id));
        return presentar(usuarioId, h);
    }

    private BusquedaHallazgos busqueda(FiltroHallazgos filtro) {
        BusquedaHallazgos busqueda = new BusquedaHallazgos()
                .especieId(filtro.especieId())
                .departamento(filtro.departamento())
                .desde(inicioDelDia(filtro.fechaDesde()))
                .hasta(inicioDelDia(filtro.fechaHasta() == null ? null : filtro.fechaHasta().plusDays(1)));
        if (filtro.especie() != null) {
            busqueda.especie(filtro.especie(), catalogo.porNombreComun(filtro.especie()));
        }
        if (filtro.texto() != null) {
            for (String palabra : filtro.texto().split("\\s+")) {
                busqueda.palabra(palabra, catalogo.porNombreComun(palabra));
            }
        }
        return busqueda;
    }

    private static Instant inicioDelDia(LocalDate dia) {
        return dia == null ? null : dia.atStartOfDay(ZONA_COLOMBIA).toInstant();
    }

    private VistaHallazgoPublico presentar(String usuarioId, HallazgoLectura h) {
        EspecieInfo especie = catalogo.porId(h.getEspecieId())
                .or(() -> catalogo.porNombreCientifico(h.getEspecie()))
                .orElse(null);
        String categoria = especie == null ? null : especie.estadoConservacion();
        NivelVisibilidad nivel = autorizacion.visibilidad(usuarioId, categoria, h.getEspecieId());
        return PresentadorUbicacion.presentar(h, especie, nivel);
    }
}
