package com.example.divisaodespesas.dto.grupo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GrupoResponseDTO {

    private Integer id;
    private String nomeGrupo;
    private Boolean situacao;
    private String moeda;

}
