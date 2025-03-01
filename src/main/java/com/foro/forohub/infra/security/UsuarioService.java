package com.foro.forohub.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.foro.forohub.domain.usuarios.DatosRegistroUsuario;
import com.foro.forohub.domain.usuarios.Usuario;
import com.foro.forohub.domain.usuarios.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(DatosRegistroUsuario datosRegistroUsuario) {
        usuarioRepository.findByLogin(datosRegistroUsuario.login())
                .ifPresent(u -> {
                    throw new RuntimeException("El usuario ya existe");
                });

        // Encriptar la contraseña
        String claveEncriptada = passwordEncoder.encode(datosRegistroUsuario.clave());

        Usuario usuario = new Usuario(datosRegistroUsuario);
        usuario.setPass(claveEncriptada);

        return usuarioRepository.save(usuario);

    }


}
