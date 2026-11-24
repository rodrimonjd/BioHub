package co.edu.unbosque.biohub.consulta.visibilidad;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.consulta.PropiedadesDePrueba;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClienteAutorizacionHttpTest {

    @Test
    void siAutorizacionNoRespondeSeAplicaElNivelMasRestrictivo() {
        ClienteAutorizacionHttp cliente = new ClienteAutorizacionHttp(
                PropiedadesDePrueba.con("http://localhost:1", "consulta-publica.hallazgos.dlq"));
        assertEquals(NivelVisibilidad.DEPARTAMENTO, cliente.visibilidad("admin", "LC", null));
    }
}
