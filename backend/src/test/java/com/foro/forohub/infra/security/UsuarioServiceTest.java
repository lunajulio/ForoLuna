package com.foro.forohub.infra.security;

import com.foro.forohub.domain.usuarios.DatosRegistroUsuario;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.domain.usuarios.UsuarioRepository;
import com.foro.forohub.infra.errores.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private final DatosRegistroUsuario datos = new DatosRegistroUsuario("Ana", "ana", "clave123");

    @Test
    @DisplayName("guarda el usuario con la contraseña cifrada")
    void registraConClaveCifrada() {
        when(usuarioRepository.findByLogin("ana")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.registrarUsuario(datos);

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getPassword()).isEqualTo("HASH");
        assertThat(guardado.getValue().getLogin()).isEqualTo("ana");
        assertThat(guardado.getValue().getNombre()).isEqualTo("Ana");
        assertThat(resultado).isSameAs(guardado.getValue());
    }

    @Test
    @DisplayName("lanza 409 si el login ya existe y no guarda nada")
    void loginDuplicado() {
        when(usuarioRepository.findByLogin("ana")).thenReturn(Optional.of(new Usuario(datos)));

        assertThatThrownBy(() -> usuarioService.registrarUsuario(datos))
                .isInstanceOf(ValidacionException.class)
                .hasMessage("El usuario ya existe")
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }
}
