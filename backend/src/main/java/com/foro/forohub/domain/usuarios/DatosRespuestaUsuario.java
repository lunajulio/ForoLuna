package com.foro.forohub.domain.usuarios;

public record DatosRespuestaUsuario(
        Long id,
        String nombre,
        String login
) {
    public DatosRespuestaUsuario(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getUsername() // usa getUsername() porque viene de UserDetails
        );
    }

}
