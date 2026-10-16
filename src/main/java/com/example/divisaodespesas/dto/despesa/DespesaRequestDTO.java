package com.example.divisaodespesas.dto.despesa;

import com.example.divisaodespesas.database.model.enums.TipoDivisao;
import com.example.divisaodespesas.dto.parcela.ParcelaRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DespesaRequestDTO {
    @NotBlank
    private String descricao;
    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 2)
    private BigDecimal valorTotal;
    @NotNull
    private TipoDivisao tipoDivisao;
    @NotNull
    @Positive
    private Integer grupoId;
    @NotNull
    @Positive
    private Integer idPagador;
    private Set<@NotNull @Positive Integer> beneficiariosId;
    @Valid
    private List<ParcelaRequestDTO> parcelas;
}
