package com.foro.forohub.controller;

import org.springframework.web.bind.annotation.RestController;

import com.foro.forohub.domain.usuarios.DatosRegistroUsuario;
import com.foro.forohub.domain.usuarios.DatosRespuestaUsuario;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.infra.security.UsuarioService;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/usuarios")
@SecurityRequirement(name = "bearer-key")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<DatosRespuestaUsuario> registrarUsuario(@RequestBody @Valid DatosRegistroUsuario datosRegistroUsuario) {
        Usuario usuario = usuarioService.registrarUsuario(datosRegistroUsuario);
        return ResponseEntity.ok(new DatosRespuestaUsuario(usuario));
    }



}
