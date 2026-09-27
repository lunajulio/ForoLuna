package com.foro.forohub.domain;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.curso.CursoRepository;
import com.foro.forohub.domain.curso.DatosCurso;
import com.foro.forohub.domain.topico.DatosSubirTopico;
import com.foro.forohub.domain.topico.Topico;
import com.foro.forohub.domain.topico.TopicoRepository;
import com.foro.forohub.domain.usuarios.DatosRegistroUsuario;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.domain.usuarios.UsuarioRepository;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de las consultas de los repositorios contra H2 con el esquema real de Flyway.
 * Cada test corre en una transacción que se revierte al terminar.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Repositorios")
class RepositoriosTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;
    private Curso curso;

    @BeforeEach
    void setUp() {
        usuario = em.persist(new Usuario(new DatosRegistroUsuario("Ana", unico("ana"), "hash")));
        curso = em.persist(new Curso(unico("Curso"), "Backend"));
    }

    @Test
    @DisplayName("findByStatusTrue solo devuelve tópicos activos")
    void soloActivos() {
        Topico activo = persistirTopico(unico("Activo"));
        Topico inactivo = persistirTopico(unico("Inactivo"));
        inactivo.deshabilitarTopico();
        em.flush();

        var pagina = topicoRepository.findByStatusTrue(PageRequest.of(0, 1000));

        assertThat(pagina.getContent()).contains(activo).doesNotContain(inactivo);
    }

    @Test
    @DisplayName("findByIdAndStatusTrue ignora tópicos eliminados")
    void porIdActivo() {
        Topico topico = persistirTopico(unico("Por id"));
        assertThat(topicoRepository.findByIdAndStatusTrue(topico.getId())).contains(topico);

        topico.deshabilitarTopico();
        em.flush();
        assertThat(topicoRepository.findByIdAndStatusTrue(topico.getId())).isEmpty();
    }

    @Test
    @DisplayName("existsByTituloAndStatusTrue no cuenta tópicos eliminados")
    void existeTituloActivo() {
        String titulo = unico("Existe");
        Topico topico = persistirTopico(titulo);
        assertThat(topicoRepository.existsByTituloAndStatusTrue(titulo)).isTrue();

        topico.deshabilitarTopico();
        em.flush();
        assertThat(topicoRepository.existsByTituloAndStatusTrue(titulo)).isFalse();
        assertThat(topicoRepository.existsByTituloAndStatusTrue(unico("NoExiste"))).isFalse();
    }

    @Test
    @DisplayName("findByNombreAndCategoria encuentra el curso exacto")
    void cursoPorNombreYCategoria() {
        assertThat(cursoRepository.findByNombreAndCategoria(curso.getNombre(), "Backend")).contains(curso);
        assertThat(cursoRepository.findByNombreAndCategoria(curso.getNombre(), "Frontend")).isEmpty();
    }

    @Test
    @DisplayName("la base impide cursos duplicados (nombre + categoría)")
    void cursoUnico() {
        assertThatThrownBy(() -> em.persistAndFlush(new Curso(curso.getNombre(), "Backend")))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("la base impide logins duplicados")
    void loginUnico() {
        assertThatThrownBy(() -> em.persistAndFlush(
                new Usuario(new DatosRegistroUsuario("Otra", usuario.getLogin(), "hash"))))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("findByLogin y buscarPorLogin encuentran al usuario")
    void usuarioPorLogin() {
        assertThat(usuarioRepository.findByLogin(usuario.getLogin())).contains(usuario);
        assertThat(usuarioRepository.buscarPorLogin(usuario.getLogin())).contains(usuario);
        assertThat(usuarioRepository.buscarPorLogin("nadie-" + UUID.randomUUID())).isEmpty();
    }

    private Topico persistirTopico(String titulo) {
        return em.persistAndFlush(new Topico(
                new DatosSubirTopico(titulo, "Mensaje", new DatosCurso(curso.getNombre(), curso.getCategoria())),
                usuario, curso));
    }

    private static String unico(String prefijo) {
        return prefijo + "-" + UUID.randomUUID();
    }
}
