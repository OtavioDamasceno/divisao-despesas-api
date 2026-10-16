package com.example.divisaodespesas.service;

import com.example.divisaodespesas.database.model.DespesaEntity;
import com.example.divisaodespesas.database.model.ParcelaEntity;
import com.example.divisaodespesas.database.repository.IDespesaRepository;
import com.example.divisaodespesas.database.repository.IGrupoRepository;
import com.example.divisaodespesas.database.repository.IParcelaRepository;
import com.example.divisaodespesas.database.repository.IPessoaRepository;
import com.example.divisaodespesas.dto.despesa.DespesaRequestDTO;
import com.example.divisaodespesas.dto.despesa.DespesaResponseDTO;
import com.example.divisaodespesas.dto.parcela.ParcelaResponseDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DespesaService {

    private final IDespesaRepository despesaRepository;
    private final IGrupoRepository grupoRepository;
    private final IPessoaRepository pessoaRepository;
    private final IParcelaRepository parcelaRepository;
    private final ParcelaService parcelaService;

    @Transactional(readOnly = true)
    public DespesaResponseDTO buscaPorId(Integer id) throws BadRequestException {
        DespesaEntity despesa = despesaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Despesa nao encontrada"));
        Set<ParcelaResponseDTO> parcelasResponse = new HashSet<>();

        for (ParcelaEntity parcela : despesa.getParcelas()) {
            ParcelaResponseDTO dto = new ParcelaResponseDTO();
            dto.setId(parcela.getId());
            dto.setValor_da_parcela(parcela.getValor_da_parcela());
            dto.setDespesaId(despesa.getId());
            dto.setPessoaId(parcela.getPessoa().getId());

            parcelasResponse.add(dto);
        }
        DespesaResponseDTO despesaResponse = new DespesaResponseDTO(
                despesa.getId(),
                despesa.getDescricao(),
                despesa.getValorTotal(),
                despesa.getTipoDivisao(),
                despesa.getGrupo().getId(),
                despesa.getPagador().getId(),
                parcelasResponse
        );
        return despesaResponse;
    }

    // public void criarDespesa(DespesaRequestDTO despesaRequestDTO) throws BadRequestException {}

}
