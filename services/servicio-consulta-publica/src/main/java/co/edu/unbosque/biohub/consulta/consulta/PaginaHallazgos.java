package co.edu.unbosque.biohub.consulta.consulta;

import java.util.List;

public record PaginaHallazgos(List<VistaHallazgoPublico> contenido, int pagina, int tamano,
                              long totalElementos, int totalPaginas) {}
