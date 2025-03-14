package com.foro.forohub.domain.respuesta;

import com.foro.forohub.domain.curso.DatosCurso;
import com.foro.forohub.domain.topico.Topico;

import java.time.LocalDateTime;
import java.util.List;

public record DatosRespuestaRespuesta(
        Long Id,
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
