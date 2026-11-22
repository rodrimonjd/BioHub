package co.edu.unbosque.biohub.taxonomia.evento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.biohub.taxonomia.ficha.FichaRepository;
import co.edu.unbosque.biohub.taxonomia.ficha.FichaTaxonomica;

/**
 * Cada 5 segundos busca fichas con eventos pendientes, los publica en orden
 * y los quita de la ficha. Si RabbitMQ no responde, lo intenta en la siguiente vuelta.
 */
@Component
public class RelayEventos {

    private static final Logger log = LoggerFactory.getLogger(RelayEventos.class);

    private final FichaRepository fichas;
    private final PublicadorEventos publicador;

    public RelayEventos(FichaRepository fichas, PublicadorEventos publicador) {
        this.fichas = fichas;
        this.publicador = publicador;
    }

    @Scheduled(fixedDelayString = "${biohub.eventos.intervalo-ms}")
    public void publicarPendientes() {
        for (FichaTaxonomica ficha : fichas.buscarConEventosPendientes()) {
            int publicados = 0;
            boolean fallo = false;
            for (EventoPendiente evento : ficha.getEventosPendientes()) {
                try {
                    publicador.publicar(evento.getTipo(), evento.getPayload());
                    publicados++;
                } catch (RuntimeException e) {
                    log.warn("No se pudo publicar {} de la ficha {}: {}",
                            evento.getTipo(), ficha.getId(), e.getMessage());
                    fallo = true;
                    break;
                }
            }

            if (publicados > 0) {
                ficha.quitarEventosPublicados(publicados);
                try {
                    fichas.save(ficha);
                } catch (OptimisticLockingFailureException e) {
                    // Alguien edito la ficha al mismo tiempo; se revisa en la siguiente vuelta.
                    log.warn("La ficha {} cambio mientras se publicaban sus eventos", ficha.getId());
                }
            }
            if (fallo) {
                return;
            }
        }
    }
}
