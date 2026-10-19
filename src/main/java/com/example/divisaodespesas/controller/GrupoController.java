package com.example.divisaodespesas.controller;

import com.example.divisaodespesas.dto.grupo.GrupoRequestDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import com.example.divisaodespesas.service.GrupoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupo")
@RequiredArgsConstructor
@Validated
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void criarGrupo(@Valid @RequestBody GrupoRequestDTO grupoRequestDTO) throws BadRequestException {
        grupoService.criarGrupo(grupoRequestDTO);
    }
}
