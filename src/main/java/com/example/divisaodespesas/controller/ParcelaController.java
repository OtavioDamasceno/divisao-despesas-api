package com.example.divisaodespesas.controller;

import com.example.divisaodespesas.dto.ParcelaDTO;
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

    @GetMapping("/{parcelaid}")
    @ResponseStatus(HttpStatus.OK)
    public ParcelaDTO buscarPorId(@Valid @PathVariable Integer parcelaId) throws NotFoundException {
        return parcelaService.buscarPorId(parcelaId);
    }

    @GetMapping("/pessoa/{pessoaid}")
    @ResponseStatus(HttpStatus.OK)
    public Set<ParcelaDTO> buscarPorPessoa(@Valid @PathVariable Integer pessoaId) throws NotFoundException {
        return parcelaService.listarPorPessoa(pessoaId);
    }

    @GetMapping("/despesas/{despesaid}")
    @ResponseStatus(HttpStatus.OK)
    public Set<ParcelaDTO> listarPorDespesa(@Valid @PathVariable Integer despesaId) throws NotFoundException {
        return parcelaService.listarPorDespesa(despesaId);
    }
}
