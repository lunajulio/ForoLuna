package com.foro.forohub.domain.usuarios;

import jakarta.validation.constraints.NotBlank;

public record DatosRegistroUsuario(
        @NotBlank
        String nombre,
        @NotBlank
        String login,
        @NotBlank
        String clave
) {
}
