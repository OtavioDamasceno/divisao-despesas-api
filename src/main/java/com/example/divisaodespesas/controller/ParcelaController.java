package com.example.divisaodespesas.controller;

import com.example.divisaodespesas.dto.parcela.ParcelaRequestDTO;
import com.example.divisaodespesas.exception.NotFoundException;
import com.example.divisaodespesas.service.ParcelaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/parcela")
@RequiredArgsConstructor
@Validated
public class ParcelaController {

    private final ParcelaService parcelaService;

    @GetMapping("/{parcelaId}")
    @ResponseStatus(HttpStatus.OK)
    public ParcelaRequestDTO buscarPorId(@Valid @PathVariable Integer parcelaId) throws NotFoundException {
        return parcelaService.buscarPorId(parcelaId);
    }

    @GetMapping("/pessoa/{pessoaId}")
    @ResponseStatus(HttpStatus.OK)
    public Set<ParcelaRequestDTO> buscarPorPessoa(@Valid @PathVariable Integer pessoaId) throws NotFoundException {
        return parcelaService.listarPorPessoa(pessoaId);
    }

    @GetMapping("/despesas/{despesaId}")
    @ResponseStatus(HttpStatus.OK)
    public Set<ParcelaRequestDTO> listarPorDespesa(@Valid @PathVariable Integer despesaId) throws NotFoundException {
        return parcelaService.listarPorDespesa(despesaId);
    }
}
