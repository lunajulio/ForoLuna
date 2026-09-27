package com.foro.forohub.controller;

import com.foro.forohub.domain.usuarios.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UsuarioController (POST /usuarios)")
class UsuarioControllerTest extends IntegrationTestSupport {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("registra al usuario sin exponer la contraseña en la respuesta")
    void registro() throws Exception {
        String login = unico("nuevo");

        mvc.perform(json(post("/usuarios"), usuarioJson(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Test"))
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.clave").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("guarda la contraseña cifrada con BCrypt")
    void claveCifrada() throws Exception {
        String login = unico("cifrado");
        registrar(login);

        String guardada = usuarioRepository.buscarPorLogin(login).orElseThrow().getPassword();
        assertThat(guardada).isNotEqualTo(CLAVE).startsWith("$2");
        assertThat(passwordEncoder.matches(CLAVE, guardada)).isTrue();
    }

    @Test
    @DisplayName("no requiere token")
    void publico() throws Exception {
        mvc.perform(json(post("/usuarios"), usuarioJson(unico("publico"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("un login repetido devuelve 409")
    void duplicado() throws Exception {
        String login = unico("dup");
        registrar(login);

        mvc.perform(json(post("/usuarios"), usuarioJson(login)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El usuario ya existe"));
    }

    @Test
    @DisplayName("campos vacíos devuelven 400 indicando cuáles fallan")
    void camposVacios() throws Exception {
        mvc.perform(json(post("/usuarios"), "{\"nombre\":\"\",\"login\":\"\",\"clave\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("nombre"),
                        org.hamcrest.Matchers.containsString("login"),
                        org.hamcrest.Matchers.containsString("clave"))));
    }
}
