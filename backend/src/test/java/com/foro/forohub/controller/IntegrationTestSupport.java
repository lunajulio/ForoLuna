package com.foro.forohub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base de los tests de integración: levanta la app completa sobre H2 en memoria
 * (ver src/test/resources/application.properties) y ofrece helpers para la API.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class IntegrationTestSupport {

    // Los tests comparten la base H2, así que cada dato lleva un sufijo único
    private static final AtomicInteger contador = new AtomicInteger();

    protected static final String CLAVE = "secret123";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected static String unico(String prefijo) {
        return prefijo + contador.incrementAndGet();
    }

    protected void registrar(String login) throws Exception {
        mvc.perform(json(post("/usuarios"), usuarioJson(login))).andExpect(status().isOk());
    }

    protected String login(String login) throws Exception {
        String body = mvc.perform(json(post("/login"), "{\"login\":\"" + login + "\",\"clave\":\"" + CLAVE + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("jwTtoken").asText();
    }

    protected String registrarYLoguear(String login) throws Exception {
        registrar(login);
        return login(login);
    }

    protected long crearTopico(String titulo, String token) throws Exception {
        return crearTopico(titulo, "Spring", "Backend", token);
    }

    protected long crearTopico(String titulo, String curso, String categoria, String token) throws Exception {
        String body = mvc.perform(json(post("/topico"), topicoJson(titulo, curso, categoria), token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    protected static String usuarioJson(String login) {
        return "{\"nombre\":\"Test\",\"login\":\"" + login + "\",\"clave\":\"" + CLAVE + "\"}";
    }

    protected static String topicoJson(String titulo) {
        return topicoJson(titulo, "Spring", "Backend");
    }

    protected static String topicoJson(String titulo, String curso, String categoria) {
        return "{\"titulo\":\"" + titulo + "\",\"mensaje\":\"m\",\"curso\":{\"nombre\":\"" + curso
                + "\",\"categoria\":\"" + categoria + "\"}}";
    }

    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body, String token) {
        return json(builder, body).header("Authorization", "Bearer " + token);
    }

    protected static MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }
}
