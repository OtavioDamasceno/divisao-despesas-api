package com.example.divisaodespesas.controller;

import com.example.divisaodespesas.database.model.ParcelaEntity;
import com.example.divisaodespesas.dto.pessoa.PessoaRequestDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import com.example.divisaodespesas.exception.NotFoundException;
import com.example.divisaodespesas.service.PessoaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/pessoa")
@RequiredArgsConstructor
@Validated
public class PessoaController {

    private final PessoaService pessoaService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void criarPessoa(@Valid @RequestBody PessoaRequestDTO pessoaRequestDTO) throws BadRequestException {
        pessoaService.criarPessoa(pessoaRequestDTO);
    }

    @GetMapping("/{email}/parcelas")
    @ResponseStatus(HttpStatus.OK)
    public Set<ParcelaEntity> getParcelas(@PathVariable String email) throws NotFoundException {
        return pessoaService.getParcelas(email);
    }
}
