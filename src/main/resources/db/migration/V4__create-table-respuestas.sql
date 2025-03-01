CREATE TABLE respuestas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    mensaje TEXT NOT NULL,
    topico_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    usuario_id BIGINT NOT NULL,
    autor VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_respuestas_topico_id FOREIGN KEY (topico_id) REFERENCES topicos (id),
    CONSTRAINT fk_respuestas_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
