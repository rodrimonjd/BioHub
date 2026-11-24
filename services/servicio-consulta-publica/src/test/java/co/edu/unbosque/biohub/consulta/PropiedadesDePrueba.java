package co.edu.unbosque.biohub.consulta;

import java.net.URI;
import java.time.Duration;

import co.edu.unbosque.biohub.consulta.configuracion.PropiedadesConsulta;

/** Configuración de servicio-consulta-publica para pruebas unitarias, con los mismos valores del application.yml. */
public final class PropiedadesDePrueba {

    private PropiedadesDePrueba() {
    }

    public static PropiedadesConsulta con(String autorizacionUrl, String colaErrores) {
        return new PropiedadesConsulta(
                new PropiedadesConsulta.Eventos("biohub.eventos", "consulta-publica.hallazgos", colaErrores),
                URI.create("http://localhost:8081"), URI.create(autorizacionUrl),
                new PropiedadesConsulta.Cache(Duration.ofMinutes(5), Duration.ofSeconds(60)));
    }
}
