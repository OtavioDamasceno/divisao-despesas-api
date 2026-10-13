package com.example.divisaodespesas.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "parcela")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ParcelaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;
    @Column(nullable = false)
    private BigDecimal valor_da_parcela;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "despesa_id", nullable = false)
    private DespesaEntity despesa;

    @ManyToOne
    @JoinColumn(name = "pessoa_id", nullable = false)
    private PessoaEntity pessoa;

}
