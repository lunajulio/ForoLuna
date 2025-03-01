package com.foro.forohub.infra.security;

import com.foro.forohub.domain.usuarios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Buscando usuario con login: " + username);

        var usuario = usuarioRepository.findByLogin(username)
                .orElseThrow(() -> {
                    System.out.println("❌ Usuario no encontrado para login: " + username);
                    return new UsernameNotFoundException("Usuario no encontrado");
                });

        System.out.println("✅ Usuario encontrado: " + usuario.getUsername());
        return usuario;
    }
}
