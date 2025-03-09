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
    private Long Id;
    private String titulo;
    private String mensaje;
    private LocalDateTime fechaCreacion;
    private Boolean status;
    private String autor;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @OneToMany(
            mappedBy = "topico",
            fetch = FetchType.EAGER,
            cascade = {CascadeType.ALL}
    )
    private List<Respuesta> respuestas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(
            name = "usuario_id"
    )
    private Usuario usuario;

    public Topico(DatosSubirTopico datosSubirTopico, String autor, Usuario usuario){
        this.titulo = datosSubirTopico.titulo();
        this.mensaje = datosSubirTopico.mensaje();
        this.fechaCreacion = datosSubirTopico.fechaCreacion();
        this.status = true;
        this.curso = datosSubirTopico.curso();
        this.autor = autor;
        this.usuario = usuario;
    }

    public void actualizarTopico(DatosActualizarTopico datosActualizarTopico){

        if (datosActualizarTopico.titulo() != null)
            this.titulo = datosActualizarTopico.titulo();

        if (datosActualizarTopico.mensaje() != null)
            this.mensaje = datosActualizarTopico.mensaje();

    }

    public void deshabilitarTopico(){
        this.status = false;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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
