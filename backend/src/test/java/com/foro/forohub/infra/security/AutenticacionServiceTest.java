package com.foro.forohub.infra.security;

import com.foro.forohub.domain.usuarios.DatosRegistroUsuario;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.domain.usuarios.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AutenticacionService")
class AutenticacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AutenticacionService autenticacionService;

    @Test
    @DisplayName("devuelve el usuario cuando el login existe")
    void encontrado() {
        Usuario usuario = new Usuario(new DatosRegistroUsuario("Ana", "ana", "x"));
        when(usuarioRepository.findByLogin("ana")).thenReturn(Optional.of(usuario));

        assertThat(autenticacionService.loadUserByUsername("ana")).isSameAs(usuario);
    }

    @Test
    @DisplayName("lanza UsernameNotFoundException cuando el login no existe")
    void noEncontrado() {
        when(usuarioRepository.findByLogin("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autenticacionService.loadUserByUsername("nadie"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
