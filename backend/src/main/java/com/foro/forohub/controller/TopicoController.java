package com.foro.forohub.controller;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.curso.CursoRepository;
import com.foro.forohub.domain.respuesta.DatosRespuestaRespuesta;
import com.foro.forohub.domain.respuesta.DatosSubirRespuesta;
import com.foro.forohub.domain.respuesta.Respuesta;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.domain.usuarios.UsuarioRepository;
import com.foro.forohub.domain.topico.*;
import com.foro.forohub.infra.errores.ValidacionException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import com.foro.forohub.infra.security.RespuestasService;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;


import java.net.URI;

@RestController
@RequestMapping("/topico")
@SecurityRequirement(name = "bearer-key")
public class TopicoController {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RespuestasService respuestaService;

    @PostMapping
    @Transactional
    public ResponseEntity<DatosRespuestaTopico> subirTopico(@RequestBody @Valid DatosSubirTopico datosSubirTopico,
                                                            UriComponentsBuilder uriComponentsBuilder,
                                                            Authentication authentication) {

        // Verificar si el título ya existe entre los tópicos activos
        if (topicoRepository.existsByTituloAndStatusTrue(datosSubirTopico.titulo())) {
            throw new ValidacionException(HttpStatus.CONFLICT, "Ya existe un tópico con ese título");
        }

        Usuario usuario = usuarioRepository.buscarPorLogin(authentication.getName())
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        String nombreCurso = datosSubirTopico.curso().nombre();
        String categoriaCurso = datosSubirTopico.curso().categoria();
        Curso curso = cursoRepository.findByNombreAndCategoria(nombreCurso, categoriaCurso)
                .orElseGet(() -> cursoRepository.save(new Curso(nombreCurso, categoriaCurso)));

        Topico topico = topicoRepository.save(new Topico(datosSubirTopico, usuario, curso));

        URI url = uriComponentsBuilder.path("/topico/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(url).body(new DatosRespuestaTopico(topico));
    }

    @PostMapping("/{topicoId}/respuestas")
    @Transactional
    public ResponseEntity<DatosRespuestaRespuesta> agregarRespuesta(
            @PathVariable Long topicoId,
            @RequestBody @Valid DatosSubirRespuesta datosRespuesta
    ) {
        Respuesta respuesta = respuestaService.registrarRespuesta(topicoId, datosRespuesta.contenido());

        return ResponseEntity.ok(new DatosRespuestaRespuesta(respuesta));
    }

    @GetMapping("/{topicoId}/respuestas")
    public ResponseEntity<List<DatosRespuestaRespuesta>> obtenerRespuestasDeTopico(@PathVariable Long topicoId) {
        List<DatosRespuestaRespuesta> respuestas = buscarTopicoActivo(topicoId).getRespuestas().stream()
            .map(DatosRespuestaRespuesta::new)
            .toList();

        return ResponseEntity.ok(respuestas);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listadoTopicos(
            @PageableDefault Pageable paginacion
        ) {
            Page<Topico> topicos = topicoRepository.findByStatusTrue(paginacion);
            Page<DatosListadoTopicos> topicosDTO = topicos.map(DatosListadoTopicos::new);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("content", topicosDTO.getContent());
            respuesta.put("totalPages", topicosDTO.getTotalPages());
            respuesta.put("totalElements", topicosDTO.getTotalElements());
            respuesta.put("pageNumber", topicosDTO.getNumber());
            respuesta.put("pageSize", topicosDTO.getSize());

            return ResponseEntity.ok(respuesta);
        }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<DatosRespuestaTopico> actualizarTopico(@PathVariable Long id,
                                                                 @RequestBody DatosActualizarTopico datosActualizarTopico,
                                                                 Authentication authentication) {
        Topico topico = buscarTopicoActivo(id);
        verificarAutor(topico, authentication);
        topico.actualizarTopico(datosActualizarTopico);
        return ResponseEntity.ok(new DatosRespuestaTopico(topico));
    }

    // DELETE LOGICO
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarTopico(@PathVariable Long id, Authentication authentication) {
        Topico topico = buscarTopicoActivo(id);
        verificarAutor(topico, authentication);
        topico.deshabilitarTopico();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosRespuestaTopico> retornaDatosTopico(@PathVariable Long id) {
        return ResponseEntity.ok(new DatosRespuestaTopico(buscarTopicoActivo(id)));
    }

    private Topico buscarTopicoActivo(Long id) {
        return topicoRepository.findByIdAndStatusTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));
    }

    private void verificarAutor(Topico topico, Authentication authentication) {
        if (!topico.esAutor(authentication.getName())) {
            throw new ValidacionException(HttpStatus.FORBIDDEN, "Solo el autor puede modificar este tópico");
        }
    }
}
