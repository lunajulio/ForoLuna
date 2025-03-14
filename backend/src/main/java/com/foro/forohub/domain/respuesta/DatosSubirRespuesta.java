package com.foro.forohub.domain.respuesta;

import jakarta.validation.constraints.NotNull;

public record DatosSubirRespuesta(
        @NotNull
        String contenido,
        @NotNull
        Long topicoId
) {

}
