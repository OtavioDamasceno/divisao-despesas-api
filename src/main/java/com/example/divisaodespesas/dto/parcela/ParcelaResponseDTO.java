package com.example.divisaodespesas.dto.parcela;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParcelaResponseDTO {
    private Integer id;
    private BigDecimal valor_da_parcela;
    private Integer despesaId;
    private Integer pessoaId;
}