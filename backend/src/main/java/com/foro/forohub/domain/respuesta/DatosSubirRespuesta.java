package com.foro.forohub.domain.respuesta;

import jakarta.validation.constraints.NotBlank;

public record DatosSubirRespuesta(
        @NotBlank
        String contenido
) {

}
