CREATE TABLE topicos (
    id BIGSERIAL NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    autor VARCHAR(255) NOT NULL,
    curso_id BIGINT,
    usuario_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_topicos_curso_id FOREIGN KEY (curso_id) REFERENCES cursos (id),
    CONSTRAINT fk_topicos_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
