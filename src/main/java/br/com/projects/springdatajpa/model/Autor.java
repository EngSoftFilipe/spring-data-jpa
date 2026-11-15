package br.com.projects.springdatajpa.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "autor")
public class Autor {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", length = 100, nullable = false)
    private String name;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate birth;

    @Column(name = "nacionalidade", length = 50, nullable = false)
    private String nationality;

}