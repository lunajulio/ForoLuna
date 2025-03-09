package com.foro.forohub.domain.usuarios;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<UserDetails> findByLogin(String login);
    @Query("SELECT u FROM Usuario u WHERE u.login = :login")
    Optional<Usuario> buscarPorLogin(String login);
}