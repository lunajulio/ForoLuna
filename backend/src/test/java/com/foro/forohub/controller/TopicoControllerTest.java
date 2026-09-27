package com.foro.forohub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.foro.forohub.domain.curso.CursoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("TopicoController")
class TopicoControllerTest extends IntegrationTestSupport {

    @Autowired
    private CursoRepository cursoRepository;

    private String autor;
    private String tokenAutor;
    private String tokenOtro;

    @BeforeEach
    void registrarUsuarios() throws Exception {
        autor = unico("autor");
        tokenAutor = registrarYLoguear(autor);
        tokenOtro = registrarYLoguear(unico("otro"));
    }

    @Nested
    @DisplayName("POST /topico")
    class Crear {

        @Test
        @DisplayName("crea el tópico con el autor del token y la fecha del servidor")
        void creaTopico() throws Exception {
            // "autor" y "fechaCreacion" enviados por el cliente deben ignorarse
            String body = "{\"titulo\":\"" + unico("Nuevo ") + "\",\"mensaje\":\"Hola\",\"autor\":\"impostor\","
                    + "\"fechaCreacion\":\"2000-01-01T00:00:00\",\"curso\":{\"nombre\":\"Java\",\"categoria\":\"Backend\"}}";

            mvc.perform(json(post("/topico"), body, tokenAutor))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.autor").value(autor))
                    .andExpect(jsonPath("$.mensaje").value("Hola"))
                    .andExpect(jsonPath("$.curso.nombre").value("Java"))
                    .andExpect(jsonPath("$.fechaCreacion").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.startsWith("2000"))))
                    .andExpect(jsonPath("$.respuestas", hasSize(0)));
        }

        @Test
        @DisplayName("reutiliza un curso existente en vez de duplicarlo")
        void reutilizaCurso() throws Exception {
            String curso = unico("Curso ");
            crearTopico(unico("A "), curso, "Backend", tokenAutor);
            crearTopico(unico("B "), curso, "Backend", tokenAutor);

            long cursosConEseNombre = cursoRepository.findAll().stream()
                    .filter(c -> c.getNombre().equals(curso))
                    .count();
            assertThat(cursosConEseNombre).isEqualTo(1);
        }

        @Test
        @DisplayName("devuelve 409 si ya existe un tópico activo con el mismo título")
        void tituloDuplicado() throws Exception {
            String titulo = unico("Duplicado ");
            crearTopico(titulo, tokenAutor);

            mvc.perform(json(post("/topico"), topicoJson(titulo), tokenAutor))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Ya existe un tópico con ese título"));
        }

        @Test
        @DisplayName("permite reutilizar el título de un tópico eliminado")
        void tituloDeTopicoEliminado() throws Exception {
            String titulo = unico("Reciclado ");
            long id = crearTopico(titulo, tokenAutor);
            mvc.perform(conToken(delete("/topico/" + id), tokenAutor)).andExpect(status().isNoContent());

            mvc.perform(json(post("/topico"), topicoJson(titulo), tokenAutor))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("devuelve 400 si falta el curso")
        void sinCurso() throws Exception {
            mvc.perform(json(post("/topico"), "{\"titulo\":\"t\",\"mensaje\":\"m\"}", tokenAutor))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("curso")));
        }

        @Test
        @DisplayName("devuelve 400 si el título está vacío")
        void tituloVacio() throws Exception {
            mvc.perform(json(post("/topico"), topicoJson(" "), tokenAutor))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("titulo")));
        }

        @Test
        @DisplayName("devuelve 400 si el JSON está mal formado")
        void jsonInvalido() throws Exception {
            mvc.perform(json(post("/topico"), "{no es json", tokenAutor))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cuerpo de la petición inválido"));
        }

        @Test
        @DisplayName("devuelve 401 sin token")
        void sinToken() throws Exception {
            mvc.perform(json(post("/topico"), topicoJson(unico("x "))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /topico")
    class Listar {

        @Test
        @DisplayName("devuelve la página pedida con los metadatos de paginación")
        void paginacion() throws Exception {
            for (int i = 0; i < 3; i++) {
                crearTopico(unico("Pagina "), tokenAutor);
            }

            mvc.perform(conToken(get("/topico?page=0&size=2"), tokenAutor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.pageNumber").value(0))
                    .andExpect(jsonPath("$.pageSize").value(2))
                    .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                    .andExpect(jsonPath("$.totalPages").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
        }

        @Test
        @DisplayName("no incluye los tópicos eliminados")
        void excluyeEliminados() throws Exception {
            long activo = crearTopico(unico("Activo "), tokenAutor);
            long eliminado = crearTopico(unico("Eliminado "), tokenAutor);
            mvc.perform(conToken(delete("/topico/" + eliminado), tokenAutor)).andExpect(status().isNoContent());

            List<Long> ids = idsDelListado();
            assertThat(ids).contains(activo).doesNotContain(eliminado);
        }

        @Test
        @DisplayName("informa el número de respuestas de cada tópico")
        void numeroRespuestas() throws Exception {
            long id = crearTopico(unico("Contador "), tokenAutor);
            responder(id, "uno", tokenOtro);
            responder(id, "dos", tokenAutor);

            String body = mvc.perform(conToken(get("/topico?size=1000"), tokenAutor))
                    .andReturn().getResponse().getContentAsString();
            for (JsonNode topico : objectMapper.readTree(body).get("content")) {
                if (topico.get("id").asLong() == id) {
                    assertThat(topico.get("numeroRespuestas").asInt()).isEqualTo(2);
                    return;
                }
            }
            throw new AssertionError("El tópico " + id + " no aparece en el listado");
        }

        @Test
        @DisplayName("devuelve 401 sin token")
        void sinToken() throws Exception {
            mvc.perform(get("/topico")).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /topico/{id}")
    class Detalle {

        @Test
        @DisplayName("incluye las respuestas como DTO, sin referencias circulares")
        void conRespuestas() throws Exception {
            long id = crearTopico(unico("Con respuestas "), tokenAutor);
            responder(id, "Hola", tokenOtro);

            mvc.perform(conToken(get("/topico/" + id), tokenAutor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.respuestas", hasSize(1)))
                    .andExpect(jsonPath("$.respuestas[0].contenido").value("Hola"))
                    .andExpect(jsonPath("$.respuestas[0].topico").doesNotExist())
                    .andExpect(jsonPath("$.respuestas[0].usuario").doesNotExist());
        }

        @Test
        @DisplayName("devuelve 404 si el tópico no existe")
        void inexistente() throws Exception {
            mvc.perform(conToken(get("/topico/999999"), tokenAutor))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Tópico no encontrado"));
        }

        @Test
        @DisplayName("devuelve 404 si el tópico fue eliminado")
        void eliminado() throws Exception {
            long id = crearTopico(unico("Borrado "), tokenAutor);
            mvc.perform(conToken(delete("/topico/" + id), tokenAutor)).andExpect(status().isNoContent());

            mvc.perform(conToken(get("/topico/" + id), tokenAutor)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /topico/{id}")
    class Actualizar {

        @Test
        @DisplayName("el autor puede editar título y mensaje")
        void autorEdita() throws Exception {
            long id = crearTopico(unico("Original "), tokenAutor);

            mvc.perform(json(put("/topico/" + id), "{\"titulo\":\"Editado\",\"mensaje\":\"Nuevo\"}", tokenAutor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.titulo").value("Editado"))
                    .andExpect(jsonPath("$.mensaje").value("Nuevo"));
        }

        @Test
        @DisplayName("los campos vacíos u omitidos no se modifican")
        void edicionParcial() throws Exception {
            String titulo = unico("Parcial ");
            long id = crearTopico(titulo, tokenAutor);

            mvc.perform(json(put("/topico/" + id), "{\"titulo\":\"  \",\"mensaje\":\"Solo mensaje\"}", tokenAutor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.titulo").value(titulo))
                    .andExpect(jsonPath("$.mensaje").value("Solo mensaje"));
        }

        @Test
        @DisplayName("otro usuario recibe 403")
        void otroUsuario() throws Exception {
            long id = crearTopico(unico("Ajeno "), tokenAutor);

            mvc.perform(json(put("/topico/" + id), "{\"titulo\":\"Hackeado\"}", tokenOtro))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Solo el autor puede modificar este tópico"));
        }

        @Test
        @DisplayName("usa el id de la URL e ignora el del cuerpo")
        void idDeLaUrl() throws Exception {
            long id1 = crearTopico(unico("Uno "), tokenAutor);
            long id2 = crearTopico(unico("Dos "), tokenAutor);

            mvc.perform(json(put("/topico/" + id2), "{\"id\":" + id1 + ",\"mensaje\":\"nuevo\"}", tokenAutor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id2));
            mvc.perform(conToken(get("/topico/" + id1), tokenAutor))
                    .andExpect(jsonPath("$.mensaje").value("m"));
        }

        @Test
        @DisplayName("devuelve 404 si el tópico no existe")
        void inexistente() throws Exception {
            mvc.perform(json(put("/topico/999999"), "{\"titulo\":\"x\"}", tokenAutor))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /topico/{id}")
    class Eliminar {

        @Test
        @DisplayName("el autor puede eliminarlo (borrado lógico)")
        void autorElimina() throws Exception {
            long id = crearTopico(unico("A borrar "), tokenAutor);

            mvc.perform(conToken(delete("/topico/" + id), tokenAutor)).andExpect(status().isNoContent());
            assertThat(idsDelListado()).doesNotContain(id);
        }

        @Test
        @DisplayName("otro usuario recibe 403 y el tópico sigue existiendo")
        void otroUsuario() throws Exception {
            long id = crearTopico(unico("Protegido "), tokenAutor);

            mvc.perform(conToken(delete("/topico/" + id), tokenOtro)).andExpect(status().isForbidden());
            mvc.perform(conToken(get("/topico/" + id), tokenAutor)).andExpect(status().isOk());
        }

        @Test
        @DisplayName("eliminar dos veces devuelve 404")
        void dobleEliminacion() throws Exception {
            long id = crearTopico(unico("Doble "), tokenAutor);

            mvc.perform(conToken(delete("/topico/" + id), tokenAutor)).andExpect(status().isNoContent());
            mvc.perform(conToken(delete("/topico/" + id), tokenAutor)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Respuestas /topico/{id}/respuestas")
    class Respuestas {

        @Test
        @DisplayName("cualquier usuario puede responder y se registra como autor")
        void responde() throws Exception {
            long id = crearTopico(unico("Pregunta "), tokenAutor);

            mvc.perform(json(post("/topico/" + id + "/respuestas"), "{\"contenido\":\"Mi respuesta\"}", tokenOtro))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.contenido").value("Mi respuesta"))
                    .andExpect(jsonPath("$.autor").value(org.hamcrest.Matchers.startsWith("otro")))
                    .andExpect(jsonPath("$.fechaCreacion").exists());
        }

        @Test
        @DisplayName("lista las respuestas del tópico en orden de creación")
        void lista() throws Exception {
            long id = crearTopico(unico("Hilo "), tokenAutor);
            responder(id, "primera", tokenOtro);
            responder(id, "segunda", tokenAutor);

            mvc.perform(conToken(get("/topico/" + id + "/respuestas"), tokenAutor))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].contenido").value("primera"))
                    .andExpect(jsonPath("$[1].contenido").value("segunda"))
                    .andExpect(jsonPath("$[1].autor").value(autor));
        }

        @Test
        @DisplayName("una respuesta vacía devuelve 400")
        void vacia() throws Exception {
            long id = crearTopico(unico("Vacia "), tokenAutor);

            mvc.perform(json(post("/topico/" + id + "/respuestas"), "{\"contenido\":\"  \"}", tokenAutor))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("responder a un tópico eliminado o inexistente devuelve 404")
        void topicoNoDisponible() throws Exception {
            long id = crearTopico(unico("Cerrado "), tokenAutor);
            mvc.perform(conToken(delete("/topico/" + id), tokenAutor)).andExpect(status().isNoContent());

            mvc.perform(json(post("/topico/" + id + "/respuestas"), "{\"contenido\":\"x\"}", tokenAutor))
                    .andExpect(status().isNotFound());
            mvc.perform(json(post("/topico/999999/respuestas"), "{\"contenido\":\"x\"}", tokenAutor))
                    .andExpect(status().isNotFound());
            mvc.perform(conToken(get("/topico/" + id + "/respuestas"), tokenAutor))
                    .andExpect(status().isNotFound());
        }
    }

    private void responder(long topicoId, String contenido, String token) throws Exception {
        mvc.perform(json(post("/topico/" + topicoId + "/respuestas"), "{\"contenido\":\"" + contenido + "\"}", token))
                .andExpect(status().isOk());
    }

    private List<Long> idsDelListado() throws Exception {
        String body = mvc.perform(conToken(get("/topico?size=1000"), tokenAutor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Long> ids = new ArrayList<>();
        objectMapper.readTree(body).get("content").forEach(t -> ids.add(t.get("id").asLong()));
        return ids;
    }
}
