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
    public Respuesta registrarRespuesta(Long topicoId, String contenido) {
        Topico topico = topicoRepository.findById(topicoId)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Usuario usuario = usuarioRepository.buscarPorLogin(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Crear la respuesta
        Respuesta respuesta = new Respuesta(
                contenido,
                topico,
                LocalDateTime.now(),
                username,
                usuario
        );

        topico.getRespuestas().add(respuesta);

        return respuestaRepository.save(respuesta);
    }
}