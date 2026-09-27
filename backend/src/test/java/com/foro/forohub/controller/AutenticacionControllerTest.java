package com.foro.forohub.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AutenticacionController (POST /login)")
class AutenticacionControllerTest extends IntegrationTestSupport {

    @Test
    @DisplayName("devuelve un JWT cuyo subject es el login del usuario")
    void loginCorrecto() throws Exception {
        String login = unico("ana");
        String token = registrarYLoguear(login);

        assertThat(JWT.decode(token).getSubject()).isEqualTo(login);
        assertThat(JWT.decode(token).getIssuer()).isEqualTo("API ForoLuna");
    }

    @Test
    @DisplayName("el token obtenido da acceso a los endpoints protegidos")
    void tokenDaAcceso() throws Exception {
        String token = registrarYLoguear(unico("acceso"));

        mvc.perform(conToken(get("/topico"), token)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("contraseña incorrecta devuelve 401 con mensaje")
    void claveIncorrecta() throws Exception {
        String login = unico("clave");
        registrar(login);

        mvc.perform(json(post("/login"), "{\"login\":\"" + login + "\",\"clave\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("usuario inexistente devuelve 401 con el mismo mensaje")
    void usuarioInexistente() throws Exception {
        mvc.perform(json(post("/login"), "{\"login\":\"noexiste\",\"clave\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
    }

    @Test
    @DisplayName("campos vacíos devuelven 400")
    void camposVacios() throws Exception {
        mvc.perform(json(post("/login"), "{\"login\":\"\",\"clave\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("un token manipulado es rechazado con 401")
    void tokenManipulado() throws Exception {
        String token = registrarYLoguear(unico("manipulado"));
        String manipulado = token.substring(0, token.length() - 4) + "abcd";

        mvc.perform(conToken(get("/topico"), manipulado)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un token firmado con otro secreto es rechazado con 401")
    void tokenOtroSecreto() throws Exception {
        String login = unico("falso");
        registrar(login);
        String falso = JWT.create()
                .withIssuer("API ForoLuna")
                .withSubject(login)
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .sign(Algorithm.HMAC256("otro-secreto"));

        mvc.perform(conToken(get("/topico"), falso)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un token expirado es rechazado con 401")
    void tokenExpirado() throws Exception {
        String login = unico("expirado");
        registrar(login);
        String expirado = JWT.create()
                .withIssuer("API ForoLuna")
                .withSubject(login)
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256("secreto-de-test"));

        mvc.perform(conToken(get("/topico"), expirado)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("la documentación Swagger es pública")
    void swaggerPublico() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
