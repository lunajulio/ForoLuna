package com.foro.forohub.domain.topico;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.respuesta.Respuesta;

import java.time.LocalDateTime;
import java.util.List;

public record DatosListadoTopicos(Long id, String titulo, String mensaje, LocalDateTime fechaCreacion, String autor, Curso curso, List<Respuesta> respuestas) {

    public DatosListadoTopicos(Topico topico) {
        this(topico.getId(), topico.getTitulo(), topico.getMensaje(), topico.getFechaCreacion(), topico.getAutor(), topico.getCurso(), topico.getRespuestas());
    }

}
