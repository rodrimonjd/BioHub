package co.edu.unbosque.biohub.autorizacion.compartido;

import co.edu.unbosque.biohub.autorizacion.compartido.Dtos.RespuestaError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<RespuestaError> noEncontrado(NoEncontradoException e) {
        return construir(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ProhibidoException.class)
    public ResponseEntity<RespuestaError> prohibido(ProhibidoException e) {
        return construir(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<RespuestaError> solicitudInvalida(Exception e) {
        return construir(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private ResponseEntity<RespuestaError> construir(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new RespuestaError(estado.value(), estado.getReasonPhrase(), mensaje));
    }
}
