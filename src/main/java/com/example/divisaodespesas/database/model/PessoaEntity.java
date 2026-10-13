package com.example.divisaodespesas.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pessoa")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PessoaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany(mappedBy = "pessoa")
    @Builder.Default
    private Set<GrupoEntity> grupo = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "pagador")
    private Set<DespesaEntity> despesasPagas = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "pessoa")
    private Set<ParcelaEntity> parcelas = new HashSet<>();
}
