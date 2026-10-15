package com.example.divisaodespesas.dto.despesa;

import com.example.divisaodespesas.database.model.enums.TipoDivisao;
import com.example.divisaodespesas.dto.parcela.ParcelaRequestDTO;
import com.example.divisaodespesas.dto.parcela.ParcelaResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class DespesaResponseDTO {
    private Integer id;
    private String descricao;
    private BigDecimal valorTotal;
    private TipoDivisao tipoDivisao;
    private Integer grupoId;
    private Integer idPagador;
    private Set<ParcelaResponseDTO> parcelas;

}
