package com.example.divisaodespesas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParcelaDTO {

    @NotBlank
    @Positive
    private BigDecimal valor_da_parcela;
    @NotEmpty
    private Integer despesaId;
    @NotEmpty
    private Integer pessoaId;

}
