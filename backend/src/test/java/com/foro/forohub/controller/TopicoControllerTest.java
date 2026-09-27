package com.foro.forohub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TopicoControllerTest {

    private static final AtomicInteger contador = new AtomicInteger();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenAutor;
    private String tokenOtro;

    @BeforeEach
    void registrarUsuarios() throws Exception {
        tokenAutor = registrarYLoguear("autor" + contador.incrementAndGet());
        tokenOtro = registrarYLoguear("otro" + contador.incrementAndGet());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/topico")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginIncorrectoDevuelve401() throws Exception {
        mvc.perform(json(post("/login"), "{\"login\":\"noexiste\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void registroDuplicadoDevuelve409() throws Exception {
        String login = "dup" + contador.incrementAndGet();
        registrar(login);
        mvc.perform(json(post("/usuarios"), usuarioJson(login)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El usuario ya existe"));
    }

    @Test
    void registroInvalidoDevuelve400() throws Exception {
        mvc.perform(json(post("/usuarios"), "{\"nombre\":\"\",\"login\":\"\",\"clave\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void topicoSinCursoDevuelve400() throws Exception {
        mvc.perform(json(post("/topico"), "{\"titulo\":\"t\",\"mensaje\":\"m\"}", tokenAutor))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tituloDuplicadoDevuelve409() throws Exception {
        String titulo = "Duplicado " + contador.incrementAndGet();
        crearTopico(titulo, tokenAutor);
        mvc.perform(json(post("/topico"), topicoJson(titulo), tokenAutor))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void detalleConRespuestasNoEsRecursivo() throws Exception {
        long id = crearTopico("Con respuestas " + contador.incrementAndGet(), tokenAutor);
        mvc.perform(json(post("/topico/" + id + "/respuestas"), "{\"contenido\":\"Hola\"}", tokenOtro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        mvc.perform(get("/topico/" + id).header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.respuestas[0].contenido").value("Hola"))
                .andExpect(jsonPath("$.respuestas[0].topico").doesNotExist());
    }

    @Test
    void respuestaVaciaDevuelve400() throws Exception {
        long id = crearTopico("Respuesta vacia " + contador.incrementAndGet(), tokenAutor);
        mvc.perform(json(post("/topico/" + id + "/respuestas"), "{\"contenido\":\"  \"}", tokenAutor))
                .andExpect(status().isBadRequest());
    }

    @Test
    void soloElAutorPuedeEditarYBorrar() throws Exception {
        long id = crearTopico("Ajeno " + contador.incrementAndGet(), tokenAutor);

        mvc.perform(json(put("/topico/" + id), "{\"titulo\":\"Hackeado\"}", tokenOtro))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/topico/" + id).header("Authorization", "Bearer " + tokenOtro))
                .andExpect(status().isForbidden());

        mvc.perform(json(put("/topico/" + id), "{\"titulo\":\"Editado\"}", tokenAutor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Editado"));
    }

    @Test
    void putUsaElIdDeLaUrl() throws Exception {
        long id1 = crearTopico("Uno " + contador.incrementAndGet(), tokenAutor);
        long id2 = crearTopico("Dos " + contador.incrementAndGet(), tokenAutor);

        mvc.perform(json(put("/topico/" + id2), "{\"id\":" + id1 + ",\"mensaje\":\"nuevo\"}", tokenAutor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id2));
        mvc.perform(get("/topico/" + id1).header("Authorization", "Bearer " + tokenAutor))
                .andExpect(jsonPath("$.mensaje").value("m"));
    }

    @Test
    void topicoBorradoDevuelve404() throws Exception {
        long id = crearTopico("Borrado " + contador.incrementAndGet(), tokenAutor);
        mvc.perform(delete("/topico/" + id).header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isNoContent());

        mvc.perform(get("/topico/" + id).header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isNotFound());
        mvc.perform(json(post("/topico/" + id + "/respuestas"), "{\"contenido\":\"x\"}", tokenAutor))
                .andExpect(status().isNotFound());
        mvc.perform(get("/topico/999999").header("Authorization", "Bearer " + tokenAutor))
                .andExpect(status().isNotFound());
    }

    private String registrarYLoguear(String login) throws Exception {
        registrar(login);
        String body = mvc.perform(json(post("/login"), "{\"login\":\"" + login + "\",\"clave\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("jwTtoken").asText();
    }

    private void registrar(String login) throws Exception {
        mvc.perform(json(post("/usuarios"), usuarioJson(login))).andExpect(status().isOk());
    }

    private long crearTopico(String titulo, String token) throws Exception {
        String body = mvc.perform(json(post("/topico"), topicoJson(titulo), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode nodo = objectMapper.readTree(body);
        return nodo.get("id").asLong();
    }

    private static String usuarioJson(String login) {
        return "{\"nombre\":\"Test\",\"login\":\"" + login + "\",\"clave\":\"secret123\"}";
    }

    private static String topicoJson(String titulo) {
        return "{\"titulo\":\"" + titulo + "\",\"mensaje\":\"m\",\"curso\":{\"nombre\":\"Spring\",\"categoria\":\"Backend\"}}";
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body, String token) {
        return json(builder, body).header("Authorization", "Bearer " + token);
    }
}
