package com.example.divisaodespesas.database.repository;

import com.example.divisaodespesas.database.model.ParcelaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IParcelaRepository extends JpaRepository<ParcelaEntity, Integer> {
}
