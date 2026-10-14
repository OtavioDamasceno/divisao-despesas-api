package com.example.divisaodespesas.service;

import com.example.divisaodespesas.database.model.DespesaEntity;
import com.example.divisaodespesas.database.model.ParcelaEntity;
import com.example.divisaodespesas.database.model.PessoaEntity;
import com.example.divisaodespesas.database.model.enums.TipoDivisao;
import com.example.divisaodespesas.database.repository.IDespesaRepository;
import com.example.divisaodespesas.database.repository.IParcelaRepository;
import com.example.divisaodespesas.database.repository.IPessoaRepository;
import com.example.divisaodespesas.dto.ParcelaDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import com.example.divisaodespesas.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ParcelaService {

    private final IParcelaRepository parcelaRepository;
    private final IPessoaRepository pessoaRepository;
    private final IDespesaRepository despesaRepository;

    //busca parcela por id, converte o entity para DTO
    public ParcelaDTO buscarPorId(Integer id) throws NotFoundException {
        ParcelaEntity parcela = parcelaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("parcela não encontrada"));

        ParcelaDTO parcelaResponse = new ParcelaDTO(parcela.getValor_da_parcela(),
                parcela.getDespesa().getId(),
                parcela.getPessoa().getId());

        return parcelaResponse;
    }

    //retorna a lista de parcelas de uma pessoa, o metodo converte para dto e adiciona a lista
    // dentro do foreach
    public Set<ParcelaDTO> listarPorPessoa(Integer pessoaId) throws NotFoundException {
        PessoaEntity pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new NotFoundException("Pessoa não encontrada"));
        Set<ParcelaDTO> parcelas = new HashSet<>();
        for (ParcelaEntity parcela : pessoa.getParcelas()) {
            ParcelaDTO dto = new ParcelaDTO(
                    parcela.getValor_da_parcela(),
                    parcela.getDespesa().getId(),
                    parcela.getPessoa().getId()
            );
            parcelas.add(dto);
        }
        return parcelas;
    }

    //mesma coisa do listarPorPessoa(). Mas é recebido o Id de uma despesa.
    public Set<ParcelaDTO> listarPorDespesa(Integer despesaId) throws NotFoundException {
        DespesaEntity despesa = despesaRepository.findById(despesaId)
                .orElseThrow(() -> new NotFoundException("Parcela não encontrada"));

        Set<ParcelaDTO> parcelas = new HashSet<>();
        for (ParcelaEntity parcela : despesa.getParcelas()) {
            ParcelaDTO dto = new ParcelaDTO(
                    parcela.getValor_da_parcela(),
                    parcela.getDespesa().getId(),
                    parcela.getPessoa().getId()
            );
            parcelas.add(dto);
        }
        return parcelas;
    }

    public Set<ParcelaEntity> gerarDivisaoIgualitaria(DespesaEntity despesa,
                                                      Set<PessoaEntity> beneficiarios) throws BadRequestException {
       //validação de erros
        if (beneficiarios.isEmpty()) {
            throw new BadRequestException("A lista de beneficiarios está vazia");
        }
        if (despesa.getTipoDivisao() != TipoDivisao.IGUALITARIA) {
            throw new BadRequestException("o tipo de divisão da despesa não é igualitario");
        }
        //calculo da divisão igualitaria
        BigDecimal valorTotal = despesa.getValorTotal();
        int quantidade = beneficiarios.size();
        BigDecimal valorParecela = valorTotal.divide(BigDecimal.valueOf(quantidade), 2,
                RoundingMode.DOWN);

        BigDecimal totalDistribuido = valorParecela.multiply(BigDecimal.valueOf(quantidade));

        BigDecimal sobra = valorTotal.subtract(totalDistribuido);
        int centavosRestantes = sobra.movePointRight(2).intValueExact();


        List<PessoaEntity> pessoasOrdenadas = new ArrayList<>(beneficiarios);
        pessoasOrdenadas.sort(Comparator.comparing(PessoaEntity::getId));

        Set<ParcelaEntity> parcelas = new LinkedHashSet<>();
        BigDecimal umCentavo = new BigDecimal("0.01");

        for (PessoaEntity pessoa : pessoasOrdenadas) {
            BigDecimal valorIndividual = valorParecela;

            if (centavosRestantes > 0) {
                valorIndividual = valorIndividual.add(umCentavo);
                centavosRestantes--;
            }

            ParcelaEntity parcela = new ParcelaEntity();
            parcela.setValor_da_parcela(valorIndividual);
            parcela.setPessoa(pessoa);
            parcela.setDespesa(despesa);

            parcelas.add(parcela);
        }
        return parcelas;
    }
}
