package com.foro.forohub.domain.topico;

import com.foro.forohub.domain.curso.Curso;
import com.foro.forohub.domain.respuesta.Respuesta;
import com.foro.forohub.domain.usuarios.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Topico")
@Table(name= "topicos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Topico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String mensaje;
    private LocalDateTime fechaCreacion;
    private Boolean status;
    private String autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @OneToMany(
        mappedBy = "topico",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Respuesta> respuestas = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Topico(DatosSubirTopico datosSubirTopico, Usuario usuario, Curso curso){
        this.titulo = datosSubirTopico.titulo();
        this.mensaje = datosSubirTopico.mensaje();
        this.fechaCreacion = LocalDateTime.now();
        this.status = true;
        this.curso = curso;
        this.autor = usuario.getLogin();
        this.usuario = usuario;
    }

    public void actualizarTopico(DatosActualizarTopico datosActualizarTopico){

        if (datosActualizarTopico.titulo() != null && !datosActualizarTopico.titulo().isBlank())
            this.titulo = datosActualizarTopico.titulo();

        if (datosActualizarTopico.mensaje() != null && !datosActualizarTopico.mensaje().isBlank())
            this.mensaje = datosActualizarTopico.mensaje();

    }

    public boolean esAutor(String login) {
        return this.autor != null && this.autor.equals(login);
    }

    public void deshabilitarTopico(){
        this.status = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public List<Respuesta> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<Respuesta> respuestas) {
        this.respuestas = respuestas;
    }

    public Usuario getUsuario() {
        return usuario;
    }


}
