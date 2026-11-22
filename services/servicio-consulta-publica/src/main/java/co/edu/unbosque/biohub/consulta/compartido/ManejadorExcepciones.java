package co.edu.unbosque.biohub.consulta.compartido;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ManejadorExcepciones extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorExcepciones.class);

    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<Object> noEncontrado(NoEncontradoException e) {
        return construir(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> solicitudInvalida(IllegalArgumentException e) {
        return construir(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> tipoInvalido(MethodArgumentTypeMismatchException e) {
        return construir(HttpStatus.BAD_REQUEST, "Valor inválido para el parámetro " + e.getName(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> errorInesperado(Exception e) {
        log.error("Error no controlado", e);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, @Nullable Object cuerpo, HttpHeaders headers,
                                                             HttpStatusCode estado, WebRequest solicitud) {
        return construir(estado, mensajePara(estado), headers);
    }

    private static String mensajePara(HttpStatusCode estado) {
        return switch (estado.value()) {
            case 404 -> "Recurso no encontrado";
            case 405 -> "Método HTTP no permitido para esta ruta";
            case 406 -> "Formato de respuesta no soportado";
            case 415 -> "Tipo de contenido no soportado; use application/json";
            default -> estado.is5xxServerError() ? "Error interno del servidor" : "Solicitud inválida";
        };
    }

    private ResponseEntity<Object> construir(HttpStatusCode estado, String mensaje, @Nullable HttpHeaders headers) {
        HttpStatus conocido = HttpStatus.resolve(estado.value());
        String error = conocido != null ? conocido.getReasonPhrase() : "Error";
        return ResponseEntity.status(estado)
                .headers(headers)
                .body(new RespuestaError(estado.value(), error, mensaje));
    }
}
