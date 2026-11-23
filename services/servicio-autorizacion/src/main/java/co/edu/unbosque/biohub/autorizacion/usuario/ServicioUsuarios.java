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

@Service
@Transactional
public class ServicioUsuarios {

    static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";
    public static final String ID_INVITADO = "invitado";

    private final UsuarioRepositorio usuarios;
    private final ServicioAutorizacion autorizacion;
    private final RolServicio roles;
    private final EventoOutboxRepositorio outbox;
    private final PasswordEncoder claves;
    private final JsonMapper json;
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

    @Transactional(readOnly = true)
    public List<ResumenUsuario> conPermiso(String permiso) {
        return usuarios.findAllByOrderByIdAsc().stream()
                .filter(u -> u.isActivo() && autorizacion.verificar(u.getId(), permiso))
                .map(u -> new ResumenUsuario(u.getId(), u.getNombre()))
                .toList();
    }

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
