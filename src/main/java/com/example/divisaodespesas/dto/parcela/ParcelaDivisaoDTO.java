package com.example.divisaodespesas.dto.parcela;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParcelaDivisaoDTO {

    @NotEmpty
    private Integer pessoaId;
    @NotEmpty
    private BigDecimal valor;
}
