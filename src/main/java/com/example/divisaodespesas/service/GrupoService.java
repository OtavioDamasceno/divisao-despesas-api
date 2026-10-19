package com.example.divisaodespesas.service;

import com.example.divisaodespesas.database.model.GrupoEntity;
import com.example.divisaodespesas.database.repository.IGrupoRepository;
import com.example.divisaodespesas.dto.despesa.DespesaResponseDTO;
import com.example.divisaodespesas.dto.grupo.GrupoRequestDTO;
import com.example.divisaodespesas.dto.grupo.GrupoResponseDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import com.example.divisaodespesas.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final IGrupoRepository grupoRepository;

    @Transactional
    public void criarGrupo(GrupoRequestDTO grupoRequestDTO) throws BadRequestException {
        if(!"BRL".equals(grupoRequestDTO.getMoeda())){
            throw new BadRequestException("A moeda deve ser BRL");
        }
        GrupoEntity grupoEntity = new GrupoEntity();
        grupoEntity.setNomeGrupo(grupoRequestDTO.getNomeGrupo());
        grupoEntity.setMoeda(grupoRequestDTO.getMoeda());
        grupoEntity.setSituacao(true);

        grupoRepository.save(grupoEntity);
    }
    
    @Transactional(readOnly = true)
    public GrupoResponseDTO buscarPorId(Integer grupoId) throws NotFoundException {
        GrupoEntity grupoEntity = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo nao encontrado"));
        GrupoResponseDTO grupoResponseDTO = new GrupoResponseDTO();
        grupoResponseDTO.setNomeGrupo(grupoEntity.getNomeGrupo());
        grupoResponseDTO.setMoeda(grupoEntity.getMoeda());
        grupoResponseDTO.setId(grupoEntity.getId());
        grupoResponseDTO.setSituacao(grupoEntity.isSituacao());

        return grupoResponseDTO;
    }
}
