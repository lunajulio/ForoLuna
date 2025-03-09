package com.foro.forohub.domain.topico;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.curso.DatosCurso;
import com.foro.forohub.domain.respuesta.Respuesta;

import java.time.LocalDateTime;
import java.util.List;

public record DatosRespuestaTopico(
        Long Id,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        String autor,
        DatosCurso curso,
        List<Respuesta> respuestas) {
    
    public DatosRespuestaTopico(Topico topico) {
        this(
            topico.getId(),
            topico.getTitulo(),
            topico.getMensaje(),
            topico.getFechaCreacion(),
            topico.getAutor(),
            new DatosCurso(
                topico.getCurso().getNombre(),    // Pasamos el nombre del curso
                topico.getCurso().getCategoria()  // Pasamos la categoría del curso
            ),
            topico.getRespuestas()
        );
    }
}