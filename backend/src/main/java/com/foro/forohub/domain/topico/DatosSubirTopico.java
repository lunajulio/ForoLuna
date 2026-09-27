package com.foro.forohub.domain.topico;

import com.foro.forohub.domain.curso.DatosCurso;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatosSubirTopico (
        @NotBlank
        String titulo,
        @NotBlank
        String mensaje,
        @NotNull
        @Valid
        DatosCurso curso) {}
