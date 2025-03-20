package com.foro.forohub.controller;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.curso.CursoRepository;
import com.foro.forohub.domain.respuesta.DatosRespuestaRespuesta;
import com.foro.forohub.domain.respuesta.DatosSubirRespuesta;
import com.foro.forohub.domain.respuesta.Respuesta;
import com.foro.forohub.domain.respuesta.RespuestaRepository;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.domain.usuarios.UsuarioRepository;
import com.foro.forohub.domain.curso.DatosCurso;
import com.foro.forohub.domain.topico.*;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import com.foro.forohub.infra.security.RespuestasService;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;


import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/topico")
@SecurityRequirement(name = "bearer-key")
public class TopicoController {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private RespuestaRepository respuestaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RespuestasService respuestaService;

    @PostMapping
    public ResponseEntity<DatosRespuestaTopico> subirTopico(@RequestBody @Valid DatosSubirTopico datosSubirTopico, UriComponentsBuilder uriComponentsBuilder) {

        // Verificar si el título ya existe en la base de datos
        if (topicoRepository.existsByTitulo(datosSubirTopico.titulo())) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }

        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String nombreAutor = authentication.getName(); // Este es el login del usuario autenticado

        Usuario usuario = usuarioRepository.buscarPorLogin(nombreAutor)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<Curso> cursoExistente = cursoRepository.findByNombreAndCategoria(
                datosSubirTopico.curso().getNombre(), 
                datosSubirTopico.curso().getCategoria()
        );
            
        Curso curso;
        if (cursoExistente.isPresent()) {
                curso = cursoExistente.get();
        } else {
                curso = datosSubirTopico.curso();
                curso = cursoRepository.save(curso); 
        }

        // Crear el tópico con el nombre del usuario autenticado como autor
        Topico topico = new Topico(datosSubirTopico, nombreAutor, usuario);
        topico.setCurso(curso);

        // Guardar el tópico en el repositorio
        topico = topicoRepository.save(topico);

        DatosRespuestaTopico datosRespuestaTopico = new DatosRespuestaTopico(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getAutor(),
                new DatosCurso(
                topico.getCurso().getNombre(),
                topico.getCurso().getCategoria()
                ),
                topico.getRespuestas()
        );

        URI url = uriComponentsBuilder.path("/topico/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(url).body(datosRespuestaTopico);
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
        Topico topico = topicoRepository.findById(topicoId)
            .orElseThrow(() -> new EntityNotFoundException("Tópico no encontrado"));
        
        List<DatosRespuestaRespuesta> respuestas = topico.getRespuestas().stream()
            .map(DatosRespuestaRespuesta::new)
            .collect(Collectors.toList());
        
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
    public ResponseEntity actualizarTopico(@RequestBody @Valid DatosActualizarTopico datosActualizarTopico) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("ID del cuerpo: " + datosActualizarTopico.id());
        System.out.println("Usuario autenticado: " + authentication.getName());
        System.out.println("Autoridades: " + authentication.getAuthorities());
    
        Topico topico = topicoRepository.getReferenceById(datosActualizarTopico.id());
        topico.actualizarTopico(datosActualizarTopico);
        return ResponseEntity.ok(new DatosRespuestaTopico(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getAutor(),
                new DatosCurso(topico.getCurso().getNombre(), topico.getCurso().getCategoria()),
                topico.getRespuestas()
        ));
    }

    // DELETE LOGICO
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity eliminarTopico(@PathVariable Long id) {
        Topico topico = topicoRepository.getReferenceById(id);
        topico.deshabilitarTopico();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosRespuestaTopico> retornaDatosTopico(@PathVariable Long id) {
        Topico topico = topicoRepository.getReferenceById(id);
        var datosTopico = new DatosRespuestaTopico(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getAutor(),
                new DatosCurso(topico.getCurso().getNombre(), topico.getCurso().getCategoria()),
                topico.getRespuestas()
        );
        return ResponseEntity.ok(datosTopico);
    }
}