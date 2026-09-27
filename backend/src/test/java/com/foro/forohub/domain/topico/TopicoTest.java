package com.foro.forohub.domain.topico;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.curso.DatosCurso;
import com.foro.forohub.domain.respuesta.DatosRespuestaRespuesta;
import com.foro.forohub.domain.respuesta.Respuesta;
import com.foro.forohub.domain.usuarios.DatosRegistroUsuario;
import com.foro.forohub.domain.usuarios.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("Topico y sus DTOs")
class TopicoTest {

    private Usuario ana;
    private Curso curso;
    private Topico topico;

    @BeforeEach
    void setUp() {
        ana = new Usuario(new DatosRegistroUsuario("Ana", "ana", "x"));
        curso = new Curso("Java", "Backend");
        topico = new Topico(new DatosSubirTopico("Título", "Mensaje", new DatosCurso("Java", "Backend")), ana, curso);
    }

    @Nested
    @DisplayName("al crearse")
    class Creacion {

        @Test
        @DisplayName("toma título, mensaje, curso y autor")
        void datos() {
            assertThat(topico.getTitulo()).isEqualTo("Título");
            assertThat(topico.getMensaje()).isEqualTo("Mensaje");
            assertThat(topico.getCurso()).isSameAs(curso);
            assertThat(topico.getUsuario()).isSameAs(ana);
            assertThat(topico.getAutor()).isEqualTo("ana");
        }

        @Test
        @DisplayName("queda activo, sin respuestas y con la fecha actual")
        void estadoInicial() {
            assertThat(topico.getStatus()).isTrue();
            assertThat(topico.getRespuestas()).isEmpty();
            assertThat(topico.getFechaCreacion()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("actualizarTopico")
    class Actualizar {

        @Test
        @DisplayName("cambia título y mensaje")
        void cambiaAmbos() {
            topico.actualizarTopico(new DatosActualizarTopico("Nuevo título", "Nuevo mensaje"));

            assertThat(topico.getTitulo()).isEqualTo("Nuevo título");
            assertThat(topico.getMensaje()).isEqualTo("Nuevo mensaje");
        }

        @ParameterizedTest(name = "título ''{0}'' no se aplica")
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("ignora un título nulo o en blanco")
        void ignoraTituloVacio(String titulo) {
            topico.actualizarTopico(new DatosActualizarTopico(titulo, "Otro mensaje"));

            assertThat(topico.getTitulo()).isEqualTo("Título");
            assertThat(topico.getMensaje()).isEqualTo("Otro mensaje");
        }

        @ParameterizedTest(name = "mensaje ''{0}'' no se aplica")
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("ignora un mensaje nulo o en blanco")
        void ignoraMensajeVacio(String mensaje) {
            topico.actualizarTopico(new DatosActualizarTopico("Otro título", mensaje));

            assertThat(topico.getTitulo()).isEqualTo("Otro título");
            assertThat(topico.getMensaje()).isEqualTo("Mensaje");
        }
    }

    @Test
    @DisplayName("deshabilitarTopico lo marca como inactivo")
    void deshabilitar() {
        topico.deshabilitarTopico();

        assertThat(topico.getStatus()).isFalse();
    }

    @Test
    @DisplayName("esAutor solo es verdadero para el login del autor")
    void esAutor() {
        assertThat(topico.esAutor("ana")).isTrue();
        assertThat(topico.esAutor("luis")).isFalse();
        assertThat(topico.esAutor(null)).isFalse();
    }

    @Nested
    @DisplayName("DTOs")
    class Dtos {

        @BeforeEach
        void agregarRespuesta() {
            Respuesta respuesta = new Respuesta("Hola", topico, LocalDateTime.of(2026, 1, 2, 3, 4), "luis", ana);
            respuesta.setId(7L);
            topico.getRespuestas().add(respuesta);
            topico.setId(1L);
        }

        @Test
        @DisplayName("DatosRespuestaTopico convierte las respuestas a DTO")
        void datosRespuestaTopico() {
            DatosRespuestaTopico dto = new DatosRespuestaTopico(topico);

            assertThat(dto.id()).isEqualTo(1L);
            assertThat(dto.autor()).isEqualTo("ana");
            assertThat(dto.curso().nombre()).isEqualTo("Java");
            assertThat(dto.curso().categoria()).isEqualTo("Backend");
            assertThat(dto.respuestas()).containsExactly(
                    new DatosRespuestaRespuesta(7L, "Hola", LocalDateTime.of(2026, 1, 2, 3, 4), "luis"));
        }

        @Test
        @DisplayName("DatosListadoTopicos cuenta las respuestas")
        void datosListado() {
            DatosListadoTopicos dto = new DatosListadoTopicos(topico);

            assertThat(dto.id()).isEqualTo(1L);
            assertThat(dto.titulo()).isEqualTo("Título");
            assertThat(dto.curso().nombre()).isEqualTo("Java");
            assertThat(dto.numeroRespuestas()).isEqualTo(1);
        }
    }
}
