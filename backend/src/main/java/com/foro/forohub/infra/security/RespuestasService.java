package com.foro.forohub.infra.security;

import com.foro.forohub.domain.respuesta.DatosSubirRespuesta;
import com.foro.forohub.domain.respuesta.Respuesta;
import com.foro.forohub.domain.respuesta.RespuestaRepository;
import com.foro.forohub.domain.topico.Topico;
import com.foro.forohub.domain.topico.TopicoRepository;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.domain.usuarios.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RespuestasService {
    @Autowired
    private RespuestaRepository respuestaRepository;

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Respuesta registrarRespuesta(DatosSubirRespuesta datosSubirRespuesta) {
        Topico topico = topicoRepository.findById(datosSubirRespuesta.topicoId())
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        // Crear la respuesta
        Respuesta respuesta = new Respuesta(
                datosSubirRespuesta.contenido(),
                topico,
                LocalDateTime.now(),
                obtenerUsuarioActual()
        );

        topico.getRespuestas().add(respuesta);

        // Guardar el tópico (que cascada la respuesta)
        topicoRepository.save(topico);

        return respuestaRepository.save(respuesta);
    }

    private String obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return username;
    }

}
