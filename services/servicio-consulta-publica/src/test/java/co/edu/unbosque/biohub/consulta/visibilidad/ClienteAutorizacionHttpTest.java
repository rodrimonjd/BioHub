package co.edu.unbosque.biohub.consulta.visibilidad;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClienteAutorizacionHttpTest {

    @Test
    void siAutorizacionNoRespondeSeAplicaElNivelMasRestrictivo() {
        // puerto 1: nadie escucha, la conexión se rechaza de inmediato
        ClienteAutorizacionHttp cliente = new ClienteAutorizacionHttp("http://localhost:1", Duration.ofSeconds(60));
        assertEquals(NivelVisibilidad.DEPARTAMENTO, cliente.visibilidad("admin", "LC"));
    }
}
