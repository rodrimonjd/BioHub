package co.edu.unbosque.biohub.historial.historial;

import co.edu.unbosque.biohub.historial.cambio.RegistroCambio;
import co.edu.unbosque.biohub.historial.cambio.RegistroCambioRepositorio;
import co.edu.unbosque.biohub.historial.cambio.TipoRegistro;
import co.edu.unbosque.biohub.historial.version.VersionRegistro;
import co.edu.unbosque.biohub.historial.version.VersionRegistroRepositorio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Repositorios en memoria (solo inserción, como la base real) para probar sin MySQL. */
class AlmacenEnMemoria {

    final List<RegistroCambio> cambios = new ArrayList<>();
    final List<VersionRegistro> versiones = new ArrayList<>();
    final RegistroCambioRepositorio repoCambios = mock(RegistroCambioRepositorio.class);
    final VersionRegistroRepositorio repoVersiones = mock(VersionRegistroRepositorio.class);

    AlmacenEnMemoria() {
        when(repoCambios.save(any())).thenAnswer(i -> { cambios.add(i.getArgument(0)); return i.getArgument(0); });
        when(repoVersiones.save(any())).thenAnswer(i -> { versiones.add(i.getArgument(0)); return i.getArgument(0); });

        when(repoCambios.existsByIdEvento(anyString())).thenAnswer(i ->
                cambios.stream().anyMatch(c -> c.getIdEvento().equals(i.getArgument(0))));
        when(repoCambios.findByTipoRegistroAndIdRegistroOrderByVersionAsc(any(), anyString())).thenAnswer(i ->
                cambiosDe(i.getArgument(0), i.getArgument(1)));
        when(repoCambios.findTopByTipoRegistroAndIdRegistroOrderByVersionDesc(any(), anyString())).thenAnswer(i ->
                cambiosDe(i.getArgument(0), i.getArgument(1)).stream().reduce((a, b) -> b));
        when(repoCambios.findByTipoRegistroAndIdRegistroAndVersion(any(), anyString(), anyInt())).thenAnswer(i ->
                cambiosDe(i.getArgument(0), i.getArgument(1)).stream()
                        .filter(c -> c.getVersion() == (int) i.getArgument(2)).findFirst());

        when(repoVersiones.findByTipoRegistroAndIdRegistroOrderByVersionAsc(any(), anyString())).thenAnswer(i ->
                versionesDe(i.getArgument(0), i.getArgument(1)));
        when(repoVersiones.findTopByTipoRegistroAndIdRegistroOrderByVersionDesc(any(), anyString())).thenAnswer(i ->
                versionesDe(i.getArgument(0), i.getArgument(1)).stream().reduce((a, b) -> b));
        when(repoVersiones.findByTipoRegistroAndIdRegistroAndVersion(any(), anyString(), anyInt())).thenAnswer(i ->
                versionesDe(i.getArgument(0), i.getArgument(1)).stream()
                        .filter(v -> v.getVersion() == (int) i.getArgument(2)).findFirst());
    }

    private List<RegistroCambio> cambiosDe(TipoRegistro tipo, String id) {
        return cambios.stream().filter(c -> c.getTipoRegistro() == tipo && c.getIdRegistro().equals(id))
                .sorted(Comparator.comparingInt(c -> c.getVersion())).toList();
    }

    private List<VersionRegistro> versionesDe(TipoRegistro tipo, String id) {
        return versiones.stream().filter(v -> v.getTipoRegistro() == tipo && v.getIdRegistro().equals(id))
                .sorted(Comparator.comparingInt(v -> v.getVersion())).toList();
    }
}
