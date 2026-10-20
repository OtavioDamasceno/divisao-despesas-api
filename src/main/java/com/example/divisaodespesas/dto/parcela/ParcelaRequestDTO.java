package com.example.divisaodespesas.dto.parcela;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParcelaRequestDTO {

    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 2)
    private BigDecimal valor_da_parcela;
    private Integer despesaId;
    @NotNull
    @Positive
    private Integer pessoaId;

}
