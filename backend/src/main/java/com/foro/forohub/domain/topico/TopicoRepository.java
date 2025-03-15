package com.foro.forohub.domain.topico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TopicoRepository extends JpaRepository<Topico, Long> {
    // Método para verificar si ya existe un tópico con un título específico
    boolean existsByTitulo(String titulo);

    // Si necesitas buscar solo entre tópicos activos
    boolean existsByTituloAndStatusTrue(String titulo);

    // Otros métodos que puedas necesitar
    @Query("SELECT t FROM Topico t " +
           "LEFT JOIN FETCH t.curso " +
           "WHERE t.status = true")
    Page<Topico> findByStatusTrue(Pageable pageable);
}
