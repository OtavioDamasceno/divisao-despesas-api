package com.example.divisaodespesas.controller;

import com.example.divisaodespesas.dto.grupo.GrupoRequestDTO;
import com.example.divisaodespesas.dto.grupo.GrupoResponseDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import com.example.divisaodespesas.exception.NotFoundException;
import com.example.divisaodespesas.service.GrupoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
@Validated
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void criarGrupo(@Valid @RequestBody GrupoRequestDTO grupoRequestDTO) throws BadRequestException {
        grupoService.criarGrupo(grupoRequestDTO);
    }

    @GetMapping("/{grupoId}")
    @ResponseStatus(HttpStatus.OK)
    public GrupoResponseDTO buscaPorId(@PathVariable Integer grupoId) throws NotFoundException {
        return grupoService.buscarPorId(grupoId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Set<GrupoResponseDTO> listarGrupos() {
        return grupoService.listarGrupos();
    }

    @PostMapping("/{grupoId}/pessoas/{pessoaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adicionarPessoa(@PathVariable("grupoId") Integer grupoId, @PathVariable("pessoaId") Integer pessoaId) throws NotFoundException, BadRequestException {
        grupoService.adicionarPessoa(pessoaId, grupoId);
    }

    @DeleteMapping("/{grupoId}/pessoas/{pessoaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerPessoa(@PathVariable("grupoId") Integer grupoId, @PathVariable("pessoaId") Integer pessoaId) throws NotFoundException, BadRequestException {
        grupoService.removerPessoa(pessoaId, grupoId);
    }

    @PatchMapping("/{grupoId}/fechamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void fecharGrupo(@PathVariable Integer grupoId) throws BadRequestException, NotFoundException {
        grupoService.fecharGrupo(grupoId);
    }
}
