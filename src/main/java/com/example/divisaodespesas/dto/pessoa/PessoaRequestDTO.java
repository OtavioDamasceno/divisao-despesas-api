package com.example.divisaodespesas.dto.pessoa;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PessoaRequestDTO {

    @NotBlank
    private String nome;
    @NotBlank
    private String email;
}
