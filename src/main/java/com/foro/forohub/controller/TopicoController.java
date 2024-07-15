package com.foro.forohub.controller;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.curso.CursoRepository;
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

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/topicos")
@SecurityRequirement(name = "bearer-key")
public class TopicoController {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @PostMapping
    public ResponseEntity<DatosRespuestaTopico> subirTopico(@RequestBody @Valid DatosSubirTopico datosSubirTopico, UriComponentsBuilder uriComponentsBuilder) {

        // Verificar si el título ya existe en la base de datos
        if (topicoRepository.existsByTitulo(datosSubirTopico.titulo())) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }

        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String nombreAutor = authentication.getName(); // Este es el login del usuario autenticado

        // Buscar el curso en la base de datos
        Optional<Curso> cursoExistente = cursoRepository.findByNombreAndCategoria(datosSubirTopico.curso().getNombre(), datosSubirTopico.curso().getCategoria());

        Curso curso;
        curso = cursoExistente.orElseGet(datosSubirTopico::curso);


        // Crear el tópico con el nombre del usuario autenticado como autor
        Topico topico = new Topico(datosSubirTopico, nombreAutor);
        topico.setCurso(curso);

        // Guardar el tópico en el repositorio
        topico = topicoRepository.save(topico);

        DatosRespuestaTopico datosRespuestaTopico = new DatosRespuestaTopico(
                topico.getId(),
                topico.getTitulo(),
                topico.getMensaje(),
                topico.getFechaCreacion(),
                topico.getAutor(),
                new DatosCurso(topico.getCurso().getNombre(), topico.getCurso().getCategoria()),
                topico.getRespuestas()
        );

        URI url = uriComponentsBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(url).body(datosRespuestaTopico);
    }

    // Otros métodos del controlador
    @GetMapping
    public ResponseEntity<Page<DatosListadoTopicos>> listadoMedicos(@PageableDefault(size = 2) Pageable paginacion) {
//        return medicoRepository.findAll(paginacion).map(DatosListadoMedico::new);
        return ResponseEntity.ok(topicoRepository.findByStatusTrue(paginacion).map(DatosListadoTopicos::new));
    }

    @PutMapping
    @Transactional
    public ResponseEntity actualizarTopico(@RequestBody @Valid DatosActualizarTopico datosActualizarTopico) {
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
