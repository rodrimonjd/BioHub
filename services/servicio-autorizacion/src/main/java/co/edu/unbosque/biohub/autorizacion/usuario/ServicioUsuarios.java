package co.edu.unbosque.biohub.autorizacion.usuario;

import co.edu.unbosque.biohub.autorizacion.autorizacion.ServicioAutorizacion;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.ResumenUsuario;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.RespuestaSesion;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.SolicitudCrearUsuario;
import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.VistaUsuario;
import co.edu.unbosque.biohub.autorizacion.compartido.NoAutenticadoException;
import co.edu.unbosque.biohub.autorizacion.evento.EventoOutbox;
import co.edu.unbosque.biohub.autorizacion.evento.EventoOutboxRepositorio;
import co.edu.unbosque.biohub.autorizacion.rol.RolServicio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Cuentas de usuario e inicio de sesión. Los roles y permisos siguen saliendo de usuario_roles. */
@Service
@Transactional
public class ServicioUsuarios {

    static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";
    /** Id que usa la interfaz para el acceso como invitado (rol CIUDADANO): no puede ser una cuenta. */
    public static final String ID_INVITADO = "invitado";

    private final UsuarioRepositorio usuarios;
    private final ServicioAutorizacion autorizacion;
    private final RolServicio roles;
    private final EventoOutboxRepositorio outbox;
    private final PasswordEncoder claves;
    private final JsonMapper json;
    /** Hash de relleno: se compara aunque el usuario no exista, para no revelar por el tiempo qué cuentas existen. */
    private final String hashRelleno;

    public ServicioUsuarios(UsuarioRepositorio usuarios, ServicioAutorizacion autorizacion, RolServicio roles,
                            EventoOutboxRepositorio outbox, PasswordEncoder claves, JsonMapper json) {
        this.usuarios = usuarios;
        this.autorizacion = autorizacion;
        this.roles = roles;
        this.outbox = outbox;
        this.claves = claves;
        this.json = json;
        this.hashRelleno = claves.encode("clave-de-relleno-no-usada");
    }

    /**
     * @param identificador id del usuario o su correo
     * @throws NoAutenticadoException con un mensaje genérico si el usuario no existe, está inactivo o la clave no coincide
     */
    @Transactional(readOnly = true)
    public RespuestaSesion iniciarSesion(String identificador, String clave) {
        String valor = identificador.trim();
        Optional<Usuario> encontrado = valor.contains("@")
                ? usuarios.findByCorreoIgnoreCase(valor)
                : usuarios.findById(valor.toLowerCase(Locale.ROOT));
        boolean claveCorrecta = claves.matches(clave, encontrado.map(u -> u.getHashClave()).orElse(hashRelleno));
        if (encontrado.isEmpty() || !claveCorrecta || !encontrado.get().isActivo()) {
            throw new NoAutenticadoException(CREDENCIALES_INVALIDAS);
        }
        Usuario u = encontrado.get();
        return new RespuestaSesion(u.getId(), u.getNombre(), u.getCorreo(),
                autorizacion.rolesDe(u.getId()), autorizacion.permisosDe(u.getId()));
    }

    /** Crea una cuenta (solo administradores, lo valida el controlador) y, si se indica, le asigna un rol. */
    public VistaUsuario crear(SolicitudCrearUsuario s) {
        String id = s.id().trim().toLowerCase(Locale.ROOT);
        String correo = s.correo().trim().toLowerCase(Locale.ROOT);
        if (ID_INVITADO.equals(id)) {
            throw new IllegalArgumentException("El id '" + ID_INVITADO + "' está reservado para el acceso de invitados");
        }
        if (usuarios.existsById(id)) {
            throw new IllegalArgumentException("Ya existe un usuario con el id " + id);
        }
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo " + correo);
        }
        usuarios.save(new Usuario(id, s.nombre().trim(), correo, claves.encode(s.clave())));
        if (s.rol() != null && !s.rol().isBlank()) {
            roles.asignarRol(id, s.rol());
        }
        emitir(id, Map.of("usuario", id, "correo", correo));
        return vista(usuarios.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<VistaUsuario> listar() {
        return usuarios.findAllByOrderByIdAsc().stream().map(u -> vista(u)).toList();
    }

    /** Cuentas activas que tienen un permiso (p. ej. quién puede autorizar una reclasificación). Solo id y nombre. */
    @Transactional(readOnly = true)
    public List<ResumenUsuario> conPermiso(String permiso) {
        return usuarios.findAllByOrderByIdAsc().stream()
                .filter(u -> u.isActivo() && autorizacion.verificar(u.getId(), permiso))
                .map(u -> new ResumenUsuario(u.getId(), u.getNombre()))
                .toList();
    }

    /** true si el id corresponde a una cuenta activa. */
    @Transactional(readOnly = true)
    public boolean esCuentaActiva(String usuarioId) {
        return usuarioId != null && usuarios.findById(usuarioId.trim().toLowerCase(Locale.ROOT))
                .map(u -> u.isActivo()).orElse(false);
    }

    private VistaUsuario vista(Usuario u) {
        return new VistaUsuario(u.getId(), u.getNombre(), u.getCorreo(), u.isActivo(), autorizacion.rolesDe(u.getId()));
    }

    private void emitir(String idAgregado, Map<String, Object> cargaUtil) {
        try {
            outbox.save(new EventoOutbox(idAgregado, "usuario.creado", json.writeValueAsString(cargaUtil)));
        } catch (JacksonException e) {
            throw new IllegalStateException("No se pudo serializar el evento usuario.creado", e);
        }
    }
}
