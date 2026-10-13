package com.example.divisaodespesas.database.repository;

import com.example.divisaodespesas.database.model.PessoaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPessoaRepository extends JpaRepository<PessoaEntity, Integer> {
    Optional<PessoaEntity> findByEmail(String email);
}
