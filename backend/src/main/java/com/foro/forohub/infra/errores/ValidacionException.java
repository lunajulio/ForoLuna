package com.foro.forohub.infra.errores;

import org.springframework.http.HttpStatus;

public class ValidacionException extends RuntimeException {

    private final HttpStatus status;

    public ValidacionException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
