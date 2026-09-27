package com.foro.forohub.infra.security;

import com.foro.forohub.domain.usuarios.DatosRegistroUsuario;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.domain.usuarios.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityFilter")
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioRepository usuarioRepository;

    private SecurityFilter filter;
    private MockHttpServletRequest request;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new SecurityFilter(tokenService, usuarioRepository);
        request = new MockHttpServletRequest();
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("con un token válido autentica al usuario con ROLE_USER")
    void tokenValido() throws Exception {
        Usuario usuario = new Usuario(new DatosRegistroUsuario("Ana", "ana", "x"));
        request.addHeader("Authorization", "Bearer token-valido");
        when(tokenService.getSubject("token-valido")).thenReturn("ana");
        when(usuarioRepository.findByLogin("ana")).thenReturn(Optional.of(usuario));

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("ana");
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(chain.getRequest()).as("la petición sigue por la cadena").isNotNull();
    }

    @Test
    @DisplayName("sin cabecera Authorization no autentica pero continúa la cadena")
    void sinCabecera() throws Exception {
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(tokenService, usuarioRepository);
    }

    @Test
    @DisplayName("ignora cabeceras que no son de tipo Bearer")
    void cabeceraNoBearer() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("con un token inválido no autentica pero continúa la cadena")
    void tokenInvalido() throws Exception {
        request.addHeader("Authorization", "Bearer malo");
        when(tokenService.getSubject("malo")).thenThrow(new RuntimeException("Token JWT inválido"));

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("si el usuario del token ya no existe no autentica")
    void usuarioBorrado() throws Exception {
        request.addHeader("Authorization", "Bearer token");
        when(tokenService.getSubject("token")).thenReturn("fantasma");
        when(usuarioRepository.findByLogin("fantasma")).thenReturn(Optional.empty());

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
