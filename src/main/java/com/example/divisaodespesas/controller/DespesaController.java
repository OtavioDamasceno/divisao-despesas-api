package com.example.divisaodespesas.controller;

import com.example.divisaodespesas.dto.despesa.DespesaRequestDTO;
import com.example.divisaodespesas.dto.despesa.DespesaResponseDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import com.example.divisaodespesas.exception.NotFoundException;
import com.example.divisaodespesas.service.DespesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/despesa")
@RequiredArgsConstructor
@Validated
public class DespesaController {

    private final DespesaService despesaService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void criarDespesa(@Valid @RequestBody DespesaRequestDTO despesaRequestDTO) throws NotFoundException, BadRequestException {
        despesaService.criarDespesa(despesaRequestDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DespesaResponseDTO buscarPorId(@PathVariable Integer id) throws NotFoundException {
        return despesaService.buscaPorId(id);
    }

}
