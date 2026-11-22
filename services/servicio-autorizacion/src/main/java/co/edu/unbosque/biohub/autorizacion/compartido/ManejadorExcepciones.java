package co.edu.unbosque.biohub.autorizacion.compartido;

import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.RespuestaError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 * Traduce todas las excepciones a {@link RespuestaError} sin exponer detalles internos
 * (nombres de clases, firmas de métodos ni trazas).
 */
@RestControllerAdvice
public class ManejadorExcepciones extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorExcepciones.class);

    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<Object> noEncontrado(NoEncontradoException e) {
        return construir(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ProhibidoException.class)
    public ResponseEntity<Object> prohibido(ProhibidoException e) {
        return construir(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> solicitudInvalida(IllegalArgumentException e) {
        return construir(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Cualquier error no previsto: se registra completo, pero al cliente solo se le da un mensaje genérico. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> errorInesperado(Exception e) {
        log.error("Error no controlado", e);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    /** Falla de @Valid: lista solo "campo: mensaje". */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
                                                                  HttpStatusCode estado, WebRequest solicitud) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(ManejadorExcepciones::describir)
                .sorted()
                .collect(Collectors.joining("; "));
        return construir(HttpStatus.BAD_REQUEST, mensaje.isEmpty() ? "Solicitud inválida" : mensaje);
    }

    /** Cuerpo ausente, JSON malformado o con tipos incorrectos. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpHeaders headers,
                                                                  HttpStatusCode estado, WebRequest solicitud) {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud falta o no es un JSON válido");
    }

    /** Resto de errores de Spring MVC (405, 415, 404 de ruta, cabecera faltante...): mismo formato de respuesta. */
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

    private static String describir(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<Object> construir(HttpStatusCode estado, String mensaje) {
        return construir(estado, mensaje, null);
    }

    private ResponseEntity<Object> construir(HttpStatusCode estado, String mensaje, @Nullable HttpHeaders headers) {
        HttpStatus conocido = HttpStatus.resolve(estado.value());
        String error = conocido != null ? conocido.getReasonPhrase() : "Error";
        return ResponseEntity.status(estado)
                .headers(headers)
                .body(new RespuestaError(estado.value(), error, mensaje));
    }
}
