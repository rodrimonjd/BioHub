package co.edu.unbosque.biohub.autorizacion.rol;

import co.edu.unbosque.biohub.autorizacion.autorizacion.ServicioAutorizacion;
import co.edu.unbosque.biohub.autorizacion.compartido.NoEncontradoException;
import co.edu.unbosque.biohub.autorizacion.compartido.ProhibidoException;
import co.edu.unbosque.biohub.autorizacion.evento.EventoOutbox;
import co.edu.unbosque.biohub.autorizacion.evento.EventoOutboxRepositorio;
import co.edu.unbosque.biohub.autorizacion.permiso.Permiso;
import co.edu.unbosque.biohub.autorizacion.permiso.PermisoRepositorio;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRol;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRolRepositorio;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RolServicio {

    public static final String PERMISO_ADMIN = "roles:administrar";

    private final RolRepositorio roles;
    private final PermisoRepositorio permisos;
    private final UsuarioRolRepositorio usuarioRoles;
    private final EventoOutboxRepositorio outbox;
    private final ServicioAutorizacion autorizacion;
    private final JsonMapper mapper;

    public RolServicio(RolRepositorio roles, PermisoRepositorio permisos,
                       UsuarioRolRepositorio usuarioRoles, EventoOutboxRepositorio outbox,
                       ServicioAutorizacion autorizacion, JsonMapper mapper) {
        this.roles = roles;
        this.permisos = permisos;
        this.usuarioRoles = usuarioRoles;
        this.outbox = outbox;
        this.autorizacion = autorizacion;
        this.mapper = mapper;
    }

    public void exigirAdmin(String solicitanteId) {
        if (solicitanteId == null || !autorizacion.verificar(solicitanteId, PERMISO_ADMIN)) {
            throw new ProhibidoException("Se requiere el permiso " + PERMISO_ADMIN);
        }
    }

    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        return roles.findAll();
    }

    public Rol crearRol(String nombre, String descripcion, Set<String> codigosPermisos) {
        String normalizado = nombre.trim().toUpperCase();
        if (roles.existsByNombre(normalizado)) {
            throw new IllegalArgumentException("El rol ya existe: " + normalizado);
        }
        Rol rol = new Rol(normalizado, descripcion);
        rol.setPermisos(resolverPermisos(codigosPermisos));
        Rol guardado = roles.save(rol);
        emitir(guardado.getNombre(), "rol.creado",
                Map.of("rol", guardado.getNombre(), "permisos", codigosPermisos == null ? Set.of() : codigosPermisos));
        return guardado;
    }

    public Rol definirPermisos(String nombreRol, Set<String> codigosPermisos) {
        Rol rol = buscarRol(nombreRol);
        rol.setPermisos(resolverPermisos(codigosPermisos));
        emitir(rol.getNombre(), "rol.permisos_actualizados",
                Map.of("rol", rol.getNombre(), "permisos", codigosPermisos == null ? Set.of() : codigosPermisos));
        return rol;
    }

    public void asignarRol(String usuarioId, String nombreRol) {
        Rol rol = buscarRol(nombreRol);
        if (usuarioRoles.findByUsuarioIdAndRolNombre(usuarioId, rol.getNombre()).isEmpty()) {
            usuarioRoles.save(new UsuarioRol(usuarioId, rol));
            emitir(usuarioId, "usuario.rol_asignado", Map.of("usuario", usuarioId, "rol", rol.getNombre()));
        }
    }

    public void revocarRol(String usuarioId, String nombreRol) {
        UsuarioRol ur = usuarioRoles.findByUsuarioIdAndRolNombre(usuarioId, nombreRol.trim().toUpperCase())
                .orElseThrow(() -> new NoEncontradoException("El usuario no tiene el rol " + nombreRol));
        usuarioRoles.delete(ur);
        emitir(usuarioId, "usuario.rol_revocado", Map.of("usuario", usuarioId, "rol", ur.getRol().getNombre()));
    }
{}
    private Rol buscarRol(String nombre) {
        return roles.findByNombre(nombre.trim().toUpperCase())
                .orElseThrow(() -> new NoEncontradoException("Rol no encontrado: " + nombre));
    }

    private Set<Permiso> resolverPermisos(Set<String> codigos) {
        if (codigos == null || codigos.isEmpty()) return new HashSet<>();
        List<Permiso> encontrados = permisos.findByCodigoIn(codigos);
        if (encontrados.size() != codigos.size()) {
            Set<String> conocidos = encontrados.stream().map(p -> p.getCodigo()).collect(Collectors.toSet());
            Set<String> desconocidos = new TreeSet<>(codigos);
            desconocidos.removeAll(conocidos);
            throw new IllegalArgumentException("Permisos inexistentes: " + desconocidos);
        }
        return new HashSet<>(encontrados);
    }

    private void emitir(String idAgregado, String tipo, Map<String, Object> cargaUtil) {
        try {
            outbox.save(new EventoOutbox(idAgregado, tipo, mapper.writeValueAsString(cargaUtil)));
        } catch (JacksonException e) {
            throw new IllegalStateException("No se pudo serializar el evento " + tipo, e);
        }
    }
}
