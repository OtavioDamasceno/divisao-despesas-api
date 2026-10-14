package com.example.divisaodespesas.dto.grupo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GrupoRequestDTO {
    @NotBlank
    private String nomeGrupo;
    @NotBlank
    private String moeda;

}
