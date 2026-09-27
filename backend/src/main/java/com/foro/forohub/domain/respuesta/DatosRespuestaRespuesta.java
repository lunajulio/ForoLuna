package com.foro.forohub.domain.respuesta;

import java.time.LocalDateTime;

public record DatosRespuestaRespuesta(
        Long id,
        String contenido,
        LocalDateTime fechaCreacion,
        String autor) {

    public DatosRespuestaRespuesta(Respuesta respuesta) {
        this(
                respuesta.getId(),
                respuesta.getMensaje(),
                respuesta.getFechaCreacion(),
                respuesta.getAutor()
        );
    }
}
