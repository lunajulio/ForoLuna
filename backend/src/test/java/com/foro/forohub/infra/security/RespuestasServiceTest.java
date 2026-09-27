package com.foro.forohub.infra.security;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.curso.DatosCurso;
import com.foro.forohub.domain.respuesta.Respuesta;
import com.foro.forohub.domain.respuesta.RespuestaRepository;
import com.foro.forohub.domain.topico.DatosSubirTopico;
import com.foro.forohub.domain.topico.Topico;
import com.foro.forohub.domain.topico.TopicoRepository;
import com.foro.forohub.domain.usuarios.DatosRegistroUsuario;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.domain.usuarios.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RespuestasService")
class RespuestasServiceTest {

    @Mock
    private RespuestaRepository respuestaRepository;

    @Mock
    private TopicoRepository topicoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RespuestasService respuestasService;

    private Usuario usuario;
    private Topico topico;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(new DatosRegistroUsuario("Luis", "luis", "x"));
        Usuario autorTopico = new Usuario(new DatosRegistroUsuario("Ana", "ana", "x"));
        topico = new Topico(
                new DatosSubirTopico("Título", "Mensaje", new DatosCurso("Java", "Backend")),
                autorTopico,
                new Curso("Java", "Backend"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("luis", null, List.of()));
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("registra la respuesta con el usuario autenticado como autor")
    void registra() {
        when(topicoRepository.findByIdAndStatusTrue(1L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.buscarPorLogin("luis")).thenReturn(Optional.of(usuario));
        when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(inv -> inv.getArgument(0));

        Respuesta respuesta = respuestasService.registrarRespuesta(1L, "Mi respuesta");

        assertThat(respuesta.getMensaje()).isEqualTo("Mi respuesta");
        assertThat(respuesta.getAutor()).isEqualTo("luis");
        assertThat(respuesta.getUsuario()).isSameAs(usuario);
        assertThat(respuesta.getTopico()).isSameAs(topico);
        assertThat(respuesta.getFechaCreacion()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
        assertThat(topico.getRespuestas()).containsExactly(respuesta);
    }

    @Test
    @DisplayName("lanza EntityNotFoundException si el tópico no existe o está eliminado")
    void topicoNoEncontrado() {
        when(topicoRepository.findByIdAndStatusTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respuestasService.registrarRespuesta(99L, "x"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Tópico no encontrado");
        verifyNoInteractions(respuestaRepository);
    }

    @Test
    @DisplayName("lanza EntityNotFoundException si el usuario autenticado ya no existe")
    void usuarioNoEncontrado() {
        when(topicoRepository.findByIdAndStatusTrue(1L)).thenReturn(Optional.of(topico));
        when(usuarioRepository.buscarPorLogin("luis")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respuestasService.registrarRespuesta(1L, "x"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Usuario no encontrado");
        verifyNoInteractions(respuestaRepository);
    }
}
