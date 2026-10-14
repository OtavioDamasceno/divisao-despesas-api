package com.example.divisaodespesas.dto.pessoa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class PessoaResponseDTO {

    @NotEmpty
    private Integer id;
    @NotBlank
    private String nome;
    @NotBlank
    private String email;
}
