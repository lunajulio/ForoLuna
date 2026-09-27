package com.foro.forohub.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.foro.forohub.domain.usuarios.DatosRegistroUsuario;
import com.foro.forohub.domain.usuarios.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@DisplayName("TokenService")
class TokenServiceTest {

    private static final String SECRETO = "secreto-unitario";

    private TokenService tokenService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRETO);
        usuario = new Usuario(new DatosRegistroUsuario("Ana", "ana", "clave"));
    }

    @Test
    @DisplayName("genera un token del que se recupera el login del usuario")
    void idaYVuelta() {
        String token = tokenService.generarToken(usuario);

        assertThat(tokenService.getSubject(token)).isEqualTo("ana");
    }

    @Test
    @DisplayName("el token expira en 5 días")
    void expiracion() {
        String token = tokenService.generarToken(usuario);

        Instant expira = JWT.decode(token).getExpiresAtAsInstant();
        assertThat(expira).isCloseTo(Instant.now().plus(5, ChronoUnit.DAYS), within(1, ChronoUnit.MINUTES));
    }

    @Test
    @DisplayName("rechaza un token con formato inválido")
    void formatoInvalido() {
        assertThatThrownBy(() -> tokenService.getSubject("esto.no.es-un-jwt"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token JWT inválido o expirado");
    }

    @Test
    @DisplayName("rechaza un token firmado con otro secreto")
    void otroSecreto() {
        String token = JWT.create().withIssuer("API ForoLuna").withSubject("ana")
                .sign(Algorithm.HMAC256("otro-secreto"));

        assertThatThrownBy(() -> tokenService.getSubject(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("rechaza un token de otro emisor")
    void otroEmisor() {
        String token = JWT.create().withIssuer("Otra API").withSubject("ana")
                .sign(Algorithm.HMAC256(SECRETO));

        assertThatThrownBy(() -> tokenService.getSubject(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("rechaza un token expirado")
    void expirado() {
        String token = JWT.create().withIssuer("API ForoLuna").withSubject("ana")
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .sign(Algorithm.HMAC256(SECRETO));

        assertThatThrownBy(() -> tokenService.getSubject(token)).isInstanceOf(RuntimeException.class);
    }
}
