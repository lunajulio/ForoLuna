package com.foro.forohub.domain.topico;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.respuesta.Respuesta;

import java.time.LocalDateTime;
import java.util.List;

public record DatosListadoTopicos(
    Long id, 
    String titulo, 
    String mensaje, 
    LocalDateTime fechaCreacion, 
    String autor, 
    DatosCursoSimplificado curso, 
    int numeroRespuestas
) {
    
    public record DatosCursoSimplificado(
        String nombre, 
        String categoria
    ) {}

    public DatosListadoTopicos(Topico topico) {
        this(
            topico.getId(), 
            topico.getTitulo(), 
            topico.getMensaje(), 
            topico.getFechaCreacion(), 
            topico.getAutor(), 
            new DatosCursoSimplificado(
                topico.getCurso().getNombre(), 
                topico.getCurso().getCategoria()
            ), 
            topico.getRespuestas().size()
        );
    }
}
