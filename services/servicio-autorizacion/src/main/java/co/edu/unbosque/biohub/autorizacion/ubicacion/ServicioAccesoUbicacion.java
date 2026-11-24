package co.edu.unbosque.biohub.autorizacion.ubicacion;

import co.edu.unbosque.biohub.autorizacion.autorizacion.ServicioAutorizacion;
import co.edu.unbosque.biohub.autorizacion.compartido.ConflictoException;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.ResumenUsuario;
import co.edu.unbosque.biohub.autorizacion.compartido.NoEncontradoException;
import co.edu.unbosque.biohub.autorizacion.compartido.ProhibidoException;
import co.edu.unbosque.biohub.autorizacion.ubicacion.CambioAccesoUbicacion.Accion;
import co.edu.unbosque.biohub.autorizacion.usuario.Usuario;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRepositorio;
import co.edu.unbosque.biohub.autorizacion.visibilidad.PoliticaVisibilidadUbicacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicioAccesoUbicacion {

    public static final String PERMISO_ADMINISTRAR = "ubicacion:administrar";

    private final AccesoUbicacionRepositorio accesos;
    private final CambioAccesoUbicacionRepositorio historial;
    private final UsuarioRepositorio usuarios;
    private final ServicioAutorizacion autorizacion;

    public ServicioAccesoUbicacion(AccesoUbicacionRepositorio accesos, CambioAccesoUbicacionRepositorio historial,
                                   UsuarioRepositorio usuarios, ServicioAutorizacion autorizacion) {
        this.accesos = accesos;
        this.historial = historial;
        this.usuarios = usuarios;
        this.autorizacion = autorizacion;
    }

    public record VistaAcceso(String usuarioId, String nombreUsuario, String especieId,
                              String otorgadoPor, Instant otorgadoEn) {}

    public record VistaCambio(Long id, String usuarioId, String nombreUsuario, String especieId, Accion accion,
                              String realizadoPor, Instant realizadoEn) {}

    public record AccesoDeUsuario(String usuarioId, boolean todas, Set<String> especies) {}

    @Transactional(readOnly = true)
    public AccesoDeUsuario accesoDe(String usuarioId) {
        String id = normalizarUsuario(usuarioId);
        if (id == null) {
            return new AccesoDeUsuario(null, false, Set.of());
        }
        boolean todas = autorizacion.verificar(id, PoliticaVisibilidadUbicacion.PERMISO_UBICACION_EXACTA);
        Set<String> especies = accesos.findByUsuarioId(id).stream()
                .map(a -> a.getEspecieId())
                .collect(Collectors.toCollection(TreeSet::new));
        return new AccesoDeUsuario(id, todas, especies);
    }

    @Transactional(readOnly = true)
    public boolean puedeVerEspecie(String usuarioId, String especieId) {
        String id = normalizarUsuario(usuarioId);
        if (id == null || especieId == null || especieId.isBlank()) {
            return false;
        }
        return accesos.findByUsuarioIdAndEspecieId(id, especieId.trim().toLowerCase(Locale.ROOT)).isPresent();
    }

    @Transactional(readOnly = true)
    public List<VistaAcceso> listar(String solicitante) {
        exigirAdministrador(solicitante);
        Map<String, String> nombres = nombresDeUsuarios();
        return accesos.findAllByOrderByUsuarioIdAscEspecieIdAsc().stream()
                .map(a -> new VistaAcceso(a.getUsuarioId(), nombres.getOrDefault(a.getUsuarioId(), a.getUsuarioId()),
                        a.getEspecieId(), a.getOtorgadoPor(), a.getOtorgadoEn()))
                .toList();
    }

    public VistaAcceso otorgar(String solicitante, String usuarioId, String especieId) {
        String autor = exigirAdministrador(solicitante);
        Usuario usuario = usuarioActivo(usuarioId);
        String especie = normalizarEspecie(especieId);
        if (autorizacion.verificar(usuario.getId(), PoliticaVisibilidadUbicacion.PERMISO_UBICACION_EXACTA)) {
            throw new ConflictoException("El usuario " + usuario.getId()
                    + " ya ve la ubicación exacta de todas las especies por su rol");
        }
        if (accesos.findByUsuarioIdAndEspecieId(usuario.getId(), especie).isPresent()) {
            throw new ConflictoException("El usuario " + usuario.getId() + " ya tiene acceso a esa especie");
        }
        AccesoUbicacion acceso = accesos.save(new AccesoUbicacion(usuario.getId(), especie, autor));
        historial.save(new CambioAccesoUbicacion(usuario.getId(), especie, Accion.OTORGADO, autor));
        return new VistaAcceso(acceso.getUsuarioId(), usuario.getNombre(), acceso.getEspecieId(),
                acceso.getOtorgadoPor(), acceso.getOtorgadoEn());
    }

    public void revocar(String solicitante, String usuarioId, String especieId) {
        String autor = exigirAdministrador(solicitante);
        String id = normalizarUsuario(usuarioId);
        String especie = normalizarEspecie(especieId);
        AccesoUbicacion acceso = accesos.findByUsuarioIdAndEspecieId(id, especie)
                .orElseThrow(() -> new NoEncontradoException("El usuario " + id + " no tiene acceso a esa especie"));
        accesos.delete(acceso);
        historial.save(new CambioAccesoUbicacion(id, especie, Accion.REVOCADO, autor));
    }

    @Transactional(readOnly = true)
    public List<VistaCambio> historial(String solicitante) {
        exigirAdministrador(solicitante);
        Map<String, String> nombres = nombresDeUsuarios();
        return historial.findAllByOrderByIdDesc().stream()
                .map(c -> new VistaCambio(c.getId(), c.getUsuarioId(),
                        nombres.getOrDefault(c.getUsuarioId(), c.getUsuarioId()), c.getEspecieId(), c.getAccion(),
                        c.getRealizadoPor(), c.getRealizadoEn()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResumenUsuario> usuarios(String solicitante) {
        exigirAdministrador(solicitante);
        return usuarios.findAllByOrderByIdAsc().stream()
                .filter(u -> u.isActivo())
                .map(u -> new ResumenUsuario(u.getId(), u.getNombre()))
                .toList();
    }

    private String exigirAdministrador(String solicitante) {
        String id = normalizarUsuario(solicitante);
        if (id == null || !autorizacion.verificar(id, PERMISO_ADMINISTRAR)) {
            throw new ProhibidoException("Se requiere el permiso " + PERMISO_ADMINISTRAR);
        }
        return id;
    }

    private Usuario usuarioActivo(String usuarioId) {
        String id = normalizarUsuario(usuarioId);
        if (id == null) {
            throw new IllegalArgumentException("usuarioId es obligatorio");
        }
        return usuarios.findById(id).filter(u -> u.isActivo())
                .orElseThrow(() -> new NoEncontradoException("No existe una cuenta activa con id " + id));
    }

    private Map<String, String> nombresDeUsuarios() {
        return usuarios.findAllByOrderByIdAsc().stream()
                .collect(Collectors.toMap(u -> u.getId(), u -> u.getNombre(), (a, b) -> a));
    }

    private static String normalizarUsuario(String usuarioId) {
        return usuarioId == null || usuarioId.isBlank() ? null : usuarioId.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizarEspecie(String especieId) {
        if (especieId == null || especieId.isBlank()) {
            throw new IllegalArgumentException("especieId es obligatorio");
        }
        try {
            return UUID.fromString(especieId.trim()).toString();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("especieId no es un identificador válido: " + especieId);
        }
    }
}
