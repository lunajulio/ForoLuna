package com.foro.forohub.infra.errores;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class TratadorDeErrores {

    public record DatosError(String message) {}

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<DatosError> tratarError404(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DatosError(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DatosError> tratarErrorValidacion(MethodArgumentNotValidException ex) {
        var mensaje = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new DatosError(mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DatosError> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new DatosError("Cuerpo de la petición inválido"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DatosError> tratarErrorAutenticacion(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DatosError("Usuario o contraseña incorrectos"));
    }

    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<DatosError> tratarErrorDeNegocio(ValidacionException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new DatosError(ex.getMessage()));
    }
}
