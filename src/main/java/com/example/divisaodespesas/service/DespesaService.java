package com.example.divisaodespesas.service;

import com.example.divisaodespesas.database.model.DespesaEntity;
import com.example.divisaodespesas.database.model.GrupoEntity;
import com.example.divisaodespesas.database.model.ParcelaEntity;
import com.example.divisaodespesas.database.model.PessoaEntity;
import com.example.divisaodespesas.database.model.enums.TipoDivisao;
import com.example.divisaodespesas.database.repository.IDespesaRepository;
import com.example.divisaodespesas.database.repository.IGrupoRepository;
import com.example.divisaodespesas.database.repository.IParcelaRepository;
import com.example.divisaodespesas.database.repository.IPessoaRepository;
import com.example.divisaodespesas.dto.despesa.DespesaRequestDTO;
import com.example.divisaodespesas.dto.despesa.DespesaResponseDTO;
import com.example.divisaodespesas.dto.parcela.ParcelaRequestDTO;
import com.example.divisaodespesas.dto.parcela.ParcelaResponseDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import com.example.divisaodespesas.exception.NotFoundException;
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
    public DespesaResponseDTO buscaPorId(Integer id) throws NotFoundException {
        DespesaEntity despesa = despesaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Despesa nao encontrada"));
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

    @Transactional(rollbackFor = Exception.class)
    public void criarDespesa(DespesaRequestDTO despesaRequestDTO) throws NotFoundException, BadRequestException {
        GrupoEntity grupoEntity = grupoRepository.findById(despesaRequestDTO.getGrupoId())
                .orElseThrow(() -> new NotFoundException("Grupo nao encontrado"));

        PessoaEntity pessoaEntity = pessoaRepository.findById(despesaRequestDTO.getIdPagador())
                .orElseThrow(() -> new NotFoundException("Pagador nao localizado"));

        if (grupoEntity.isSituacao() == false) {
            throw new BadRequestException("Não é possível cadastrar despesas em um grupo fechado");
        }
        boolean pagadorPertenceAoGrupo = false;
        for (PessoaEntity pessoa : grupoEntity.getPessoa()) {
            if (pessoa.getId().equals(pessoaEntity.getId())) {
                pagadorPertenceAoGrupo = true;
                break;
            }
        }
        if (!pagadorPertenceAoGrupo) {
            throw new BadRequestException("A pessoa inserida nao pertence ao grupo");

        }

        DespesaEntity despesaEntity = new DespesaEntity();
        despesaEntity.setDescricao(despesaRequestDTO.getDescricao());
        despesaEntity.setValorTotal(despesaRequestDTO.getValorTotal());
        despesaEntity.setTipoDivisao(despesaRequestDTO.getTipoDivisao());
        despesaEntity.setPagador(pessoaEntity);
        despesaEntity.setGrupo(grupoEntity);

        Set<ParcelaEntity> parcelas;
        if(despesaRequestDTO.getTipoDivisao() == TipoDivisao.IGUALITARIA){
            if (despesaRequestDTO.getBeneficiariosId() == null
                    || despesaRequestDTO.getBeneficiariosId().isEmpty()) {
                throw new BadRequestException("Informe os beneficiários");
            }
            Set<PessoaEntity> beneficiarios = new HashSet<>();
            for(Integer pessoaId: despesaRequestDTO.getBeneficiariosId()){
                PessoaEntity beneficiario = pessoaRepository.findById(pessoaId)
                        .orElseThrow(() -> new NotFoundException("Beneficiario nao encontrado"));
                beneficiarios.add(beneficiario);
            }
               parcelas = parcelaService.gerarDivisaoIgualitaria(despesaEntity, beneficiarios);

        }else if(despesaRequestDTO.getTipoDivisao() == TipoDivisao.PERSONALIZADA){
            if (despesaRequestDTO.getParcelas() == null
                    || despesaRequestDTO.getParcelas().isEmpty()) {
                throw new BadRequestException("Informe as parcelas da divisão personalizada");
            }

            parcelas = parcelaService.gerarDivisaoPersonalizada(despesaEntity,
                    despesaRequestDTO.getParcelas());

        }
        else{
            throw new BadRequestException("Tipo de divisao invalido");
        }
        despesaRepository.save(despesaEntity);
        parcelaRepository.saveAll(parcelas);
        despesaEntity.setParcelas(parcelas);
    }

}
