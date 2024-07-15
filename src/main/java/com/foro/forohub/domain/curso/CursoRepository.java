package com.foro.forohub.domain.curso;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    Optional<Curso> findByNombreAndCategoria(String nombre, String categoria);
}
