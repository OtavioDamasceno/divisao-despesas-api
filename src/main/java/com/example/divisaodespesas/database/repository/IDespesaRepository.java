package com.example.divisaodespesas.database.repository;

import com.example.divisaodespesas.database.model.DespesaEntity;
import com.example.divisaodespesas.database.model.ParcelaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDespesaRepository extends JpaRepository<DespesaEntity, Integer> {
}
