package br.com.projects.springdatajpa.controller.dto;

import br.com.projects.springdatajpa.model.Autor;

import java.time.LocalDate;

// Data Transfer Object
public record AutorDTO(
        String nome,
        LocalDate dataNascimento,
        String nacionalidade) {

    public Autor mapearParaAutor(){
        Autor autor = new Autor();
        autor.setNome(this.nome);
        autor.setDataNascimento(this.dataNascimento);
        autor.setNacionalidade(this.nacionalidade);
        return autor;
    }
}