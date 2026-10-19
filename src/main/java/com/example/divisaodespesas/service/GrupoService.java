package com.example.divisaodespesas.service;

import com.example.divisaodespesas.database.model.DespesaEntity;
import com.example.divisaodespesas.database.model.GrupoEntity;
import com.example.divisaodespesas.database.model.ParcelaEntity;
import com.example.divisaodespesas.database.model.PessoaEntity;
import com.example.divisaodespesas.database.repository.IGrupoRepository;
import com.example.divisaodespesas.database.repository.IPessoaRepository;
import com.example.divisaodespesas.dto.despesa.DespesaResponseDTO;
import com.example.divisaodespesas.dto.grupo.GrupoRequestDTO;
import com.example.divisaodespesas.dto.grupo.GrupoResponseDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import com.example.divisaodespesas.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final IGrupoRepository grupoRepository;
    private final IPessoaRepository pessoaRepository;

    @Transactional
    public void criarGrupo(GrupoRequestDTO grupoRequestDTO) throws BadRequestException {
        if (!"BRL".equals(grupoRequestDTO.getMoeda())) {
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

    @Transactional(readOnly = true)
    public Set<GrupoResponseDTO> listarGrupos() {
        Set<GrupoResponseDTO> gruposResponse = new HashSet<>();

        for (GrupoEntity grupo : grupoRepository.findAll()) {
            GrupoResponseDTO grupoResponseDTO = new GrupoResponseDTO();
            grupoResponseDTO.setId(grupo.getId());
            grupoResponseDTO.setSituacao(grupo.isSituacao());
            grupoResponseDTO.setMoeda(grupo.getMoeda());
            grupoResponseDTO.setNomeGrupo(grupo.getNomeGrupo());
            gruposResponse.add(grupoResponseDTO);
        }

        return gruposResponse;
    }

    @Transactional
    public void adicionarPessoa(Integer pessoaId, Integer grupoId) throws NotFoundException, BadRequestException {
        PessoaEntity pessoaEntity = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new NotFoundException("Pessoa nao encontrada"));
        GrupoEntity grupoEntity = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo nao encontrado"));

        if (!grupoEntity.isSituacao()) {
            throw new BadRequestException("O grupo se encontra fechado");
        }
        for (PessoaEntity pessoa : grupoEntity.getPessoa()) {
            if (pessoa.getId().equals(pessoaId)) {
                throw new BadRequestException("Pessoa ja adicionada ao grupo");
            }
        }
        grupoEntity.getPessoa().add(pessoaEntity);
        pessoaEntity.getGrupo().add(grupoEntity);
    }

    @Transactional
    public void removerPessoa(Integer pessoaId, Integer grupoId) throws NotFoundException, BadRequestException {
        PessoaEntity pessoaEntity = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new NotFoundException("Pessoa nao encontrada"));
        GrupoEntity grupoEntity = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo nao encontrado"));
        if (!grupoEntity.isSituacao()) {
            throw new BadRequestException("O grupo se encontra fechado");
        }
        boolean pertenceAoGrupo = false;
        for (PessoaEntity pessoa : grupoEntity.getPessoa()) {
            if (pessoa.getId().equals(pessoaId)) {
                pertenceAoGrupo = true;
                break;
            }
        }
        if (!pertenceAoGrupo) {
            throw new BadRequestException("Essa pessoa não pertence ao grupo");
        }
        for (DespesaEntity despesa : pessoaEntity.getDespesasPagas()) {
            if (despesa.getGrupo().getId().equals(grupoId)) {
                throw new BadRequestException("A pessoa possui despesas pagas neste grupo e nao pode ser removida");
            }
        }
        for (ParcelaEntity parcela : pessoaEntity.getParcelas()) {
            if (parcela.getDespesa().getGrupo().getId().equals(grupoId)) {
                throw new BadRequestException("A pessoa possui parcelas neste grupo e não pode ser removida");
            }
        }
        grupoEntity.getPessoa().remove(pessoaEntity);
        pessoaEntity.getGrupo().remove(grupoEntity);
    }

    @Transactional
    public void fecharGrupo(Integer grupoId) throws BadRequestException, NotFoundException {
        GrupoEntity grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new NotFoundException("Grupo nao encontrado"));
        if (!grupo.isSituacao()) {
            throw new BadRequestException("O grupo ja esta fechado");
        }
        for (PessoaEntity pessoa : grupo.getPessoa()) {
            BigDecimal totalPago = BigDecimal.ZERO;
            BigDecimal totalDevido = BigDecimal.ZERO;


            for (DespesaEntity despesa : pessoa.getDespesasPagas()) {
                if (despesa.getGrupo().getId().equals(grupoId)) {
                    totalPago = totalPago.add(despesa.getValorTotal());
                }
            }

            for (ParcelaEntity parcela : pessoa.getParcelas()) {
                if (parcela.getDespesa().getGrupo().getId().equals(grupoId)) {
                    totalDevido = totalDevido.add(parcela.getValor_da_parcela());
                }
            }

            BigDecimal saldo = totalPago.subtract(totalDevido);
            if (saldo.compareTo(BigDecimal.ZERO) != 0) {
                throw new BadRequestException("O grupo possui participante com saldo pendente");
            }
        }
        grupo.setSituacao(false);
    }
}
