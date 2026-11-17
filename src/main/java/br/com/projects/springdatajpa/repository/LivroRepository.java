package br.com.projects.springdatajpa.repository;

import br.com.projects.springdatajpa.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LivroRepository extends JpaRepository<Livro, UUID> {
}
