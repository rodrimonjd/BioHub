package co.edu.unbosque.biohub.registro_service.especie;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EspecieServicio {

    private final EspecieRepositorio especies;

    public EspecieServicio(EspecieRepositorio especies) {
        this.especies = especies;
    }

    @Transactional
    public Especie crear(SolicitudEspecie s) {
        String cientifico = s.nombreCientifico().trim().replaceAll("\\s+", " ");
        if (especies.existsByNombreCientificoIgnoreCase(cientifico)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una especie con el nombre científico " + cientifico);
        }
        String comun = s.nombreComun() == null || s.nombreComun().isBlank() ? null : s.nombreComun().trim();
        return especies.save(new Especie(cientifico, comun, s.estadoConservacion().trim()));
    }
}
