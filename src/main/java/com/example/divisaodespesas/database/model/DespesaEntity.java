package com.example.divisaodespesas.database.model;

import com.example.divisaodespesas.database.model.enums.TipoDivisao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "despesa")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DespesaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDivisao tipoDivisao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoEntity grupo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pagador_id", nullable = false)
    private PessoaEntity pagador;

    @Builder.Default
    @OneToMany(mappedBy = "despesa")
    private Set<ParcelaEntity> parcelas = new HashSet<>();
}
