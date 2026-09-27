package com.foro.forohub.domain.topico;

import com.foro.forohub.domain.curso.DatosCurso;
import com.foro.forohub.domain.respuesta.DatosRespuestaRespuesta;

import java.time.LocalDateTime;
import java.util.List;

public record DatosRespuestaTopico(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        String autor,
        DatosCurso curso,
        List<DatosRespuestaRespuesta> respuestas) {

    public DatosRespuestaTopico(Topico topico) {
        this(
            topico.getId(),
            topico.getTitulo(),
            topico.getMensaje(),
            topico.getFechaCreacion(),
            topico.getAutor(),
            new DatosCurso(
                topico.getCurso().getNombre(),
                topico.getCurso().getCategoria()
            ),
            topico.getRespuestas().stream().map(DatosRespuestaRespuesta::new).toList()
        );
    }
}
