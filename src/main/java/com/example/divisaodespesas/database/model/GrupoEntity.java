package com.example.divisaodespesas.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "grupo")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GrupoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String nomeGrupo;
    @Column
    @Builder.Default
    private boolean situacao = true;
    @Column(nullable = false)
    @Builder.Default
    private String moeda = "BRL";
    @ManyToMany
    @Builder.Default
    @JoinTable(
            name = "grupo_pessoa",
            joinColumns = @JoinColumn(name = "grupo_id"),
            inverseJoinColumns = @JoinColumn(name = "pessoa_id")
    )
    private Set<PessoaEntity> pessoa = new HashSet<>();

    @OneToMany(mappedBy = "grupo")
    @Builder.Default
    private Set<DespesaEntity> despesas = new HashSet<>();
}