package co.edu.unbosque.biohub.autorizacion.autorizacion;

import co.edu.unbosque.biohub.autorizacion.permiso.Permiso;
import co.edu.unbosque.biohub.autorizacion.rol.RolRepositorio;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRol;
import co.edu.unbosque.biohub.autorizacion.usuario.UsuarioRolRepositorio;
import co.edu.unbosque.biohub.autorizacion.visibilidad.NivelVisibilidad;
import co.edu.unbosque.biohub.autorizacion.visibilidad.PoliticaVisibilidadUbicacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.TreeSet;

import static java.util.stream.Collectors.toCollection;

/** Consultas de autorización: "¿puede el usuario X hacer Y?" y "¿qué ubicación puede ver?". */
@Service
@Transactional(readOnly = true)
public class ServicioAutorizacion {

    /** Rol que se asume para anónimos y usuarios sin roles asignados. */
    public static final String ROL_POR_DEFECTO = "CIUDADANO";

    private final UsuarioRolRepositorio usuarioRoles;
    private final RolRepositorio roles;
    private final PoliticaVisibilidadUbicacion politicaVisibilidad;

    public ServicioAutorizacion(UsuarioRolRepositorio usuarioRoles, RolRepositorio roles,
                                PoliticaVisibilidadUbicacion politicaVisibilidad) {
        this.usuarioRoles = usuarioRoles;
        this.roles = roles;
        this.politicaVisibilidad = politicaVisibilidad;
    }

    public Set<String> rolesDe(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            return Set.of(ROL_POR_DEFECTO);
        }
        Set<String> nombres = usuarioRoles.findByUsuarioId(usuarioId).stream()
                .map(UsuarioRol::getRol)
                .map(r -> r.getNombre())
                .collect(toCollection(TreeSet::new));
        return nombres.isEmpty() ? Set.of(ROL_POR_DEFECTO) : nombres;
    }

    public Set<String> permisosDe(String usuarioId) {
        return roles.findByNombreIn(rolesDe(usuarioId)).stream()
                .flatMap(r -> r.getPermisos().stream())
                .map(Permiso::getCodigo)
                .collect(toCollection(TreeSet::new));
    }

    public boolean verificar(String usuarioId, String permiso) {
        return permisosDe(usuarioId).contains(permiso);
    }

    public NivelVisibilidad visibilidadUbicacion(String usuarioId, String categoriaUicn) {
        return politicaVisibilidad.resolver(permisosDe(usuarioId), categoriaUicn);
    }
}
