package co.edu.unbosque.biohub.consulta.consulta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import co.edu.unbosque.biohub.consulta.especie.CatalogoEspecies;
import co.edu.unbosque.biohub.consulta.especie.EspecieInfo;
import co.edu.unbosque.biohub.consulta.lectura.BusquedaHallazgos;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLectura;
import co.edu.unbosque.biohub.consulta.lectura.HallazgoLecturaRepositorio;
import co.edu.unbosque.biohub.consulta.visibilidad.ClienteAutorizacion;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

class ServicioConsultaBusquedaTest {

    private static final EspecieInfo JAGUAR = new EspecieInfo("e2", "Panthera onca", "Jaguar", "NT");

    private final HallazgoLecturaRepositorio repositorio = mock(HallazgoLecturaRepositorio.class);
    private final CatalogoEspecies catalogo = mock(CatalogoEspecies.class);
    private final ServicioConsulta servicio = new ServicioConsulta(repositorio, catalogo, mock(ClienteAutorizacion.class));

    private final CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
    private final Root<HallazgoLectura> raiz = mock(RETURNS_DEEP_STUBS);

    @BeforeEach
    void preparar() {
        when(repositorio.findAll(ArgumentMatchers.<Specification<HallazgoLectura>>any(), any(Pageable.class))).thenReturn(Page.empty());
    }

    private static FiltroHallazgos filtro(String especie, String texto, String desde, String hasta) {
        return new FiltroHallazgos(null, null, especie, texto,
                desde == null ? null : LocalDate.parse(desde), hasta == null ? null : LocalDate.parse(hasta));
    }

    private Pageable ejecutar(FiltroHallazgos filtro) {
        servicio.listar(null, filtro, 0, 20);
        ArgumentCaptor<Specification<HallazgoLectura>> especificacion = ArgumentCaptor.captor();
        ArgumentCaptor<Pageable> pagina = ArgumentCaptor.forClass(Pageable.class);
        verify(repositorio).findAll(especificacion.capture(), pagina.capture());
        especificacion.getValue().toPredicate(raiz, mock(CriteriaQuery.class), cb);
        return pagina.getValue();
    }

    @Test
    void lasFechasSonDiasCompletosEnHoraDeColombia() {
        ejecutar(filtro(null, null, "2026-11-01", "2026-11-30"));

        verify(cb).greaterThanOrEqualTo(any(), eq(Instant.parse("2026-11-01T05:00:00Z")));
        verify(cb).lessThan(any(), eq(Instant.parse("2026-12-01T05:00:00Z")));
    }

    @Test
    void losMasRecientesVanPrimero() {
        assertThat(ejecutar(filtro(null, null, null, null)).getSort()).isEqualTo(BusquedaHallazgos.ORDEN);
    }

    @Test
    void laEspecieSeBuscaPorNombreCientificoYComun() {
        when(catalogo.porNombreComun("jaguar")).thenReturn(List.of(JAGUAR));

        ejecutar(filtro("jaguar", null, null, null));

        verify(cb).like(any(), eq("%jaguar%"), eq('!'));
        verify(cb).equal(any(), eq("e2"));
        verify(cb).equal(any(), eq("panthera onca"));
    }

    @Test
    void cadaPalabraClaveSeBuscaPorSeparado() {
        when(catalogo.porNombreComun("Jaguar")).thenReturn(List.of(JAGUAR));

        ejecutar(filtro(null, "  Jaguar   Meta ", null, null));

        verify(cb, org.mockito.Mockito.times(3)).like(any(), eq("%jaguar%"), eq('!'));
        verify(cb, org.mockito.Mockito.times(3)).like(any(), eq("%meta%"), eq('!'));
    }

    @Test
    void laPalabraClaveNoBuscaPorRegionNiPorCoordenadas() {
        ejecutar(filtro(null, "Orinoquía", null, null));

        verify(raiz, never()).get("region");
        verify(raiz, never()).get("latitud");
        verify(raiz, never()).get("longitud");
    }

    @Test
    void losComodinesDeLaBaseDeDatosSeBuscanComoTexto() {
        ejecutar(filtro(null, "100%_!", null, null));

        verify(cb, org.mockito.Mockito.times(3)).like(any(), eq("%100!%!_!!%"), eq('!'));
    }

    @Test
    void unTextoEnBlancoNoFiltra() {
        ejecutar(filtro(" ", "   ", null, null));

        verify(cb, never()).like(any(), startsWith("%"), eq('!'));
        verify(catalogo, never()).porNombreComun(any());
    }

    @Test
    void unRangoInvertidoSeRechaza() {
        assertThatThrownBy(() -> servicio.listar(null, filtro(null, null, "2026-11-30", "2026-11-01"), 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fechaDesde");
    }

    @Test
    void unTextoDemasiadoLargoSeRechaza() {
        assertThatThrownBy(() -> servicio.listar(null, filtro(null, "a".repeat(101), null, null), 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("texto");
    }
}
