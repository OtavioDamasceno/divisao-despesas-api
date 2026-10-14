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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Set<ParcelaDTO> listarPorDespesa(Integer despesaId) throws NotFoundException {
        DespesaEntity despesa = despesaRepository.findById(despesaId)
                .orElseThrow(() -> new NotFoundException("Despesa não encontrada"));

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
        validarDadosDaDespesa(despesa);
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
        validarParcelas(despesa, parcelas);
        return parcelas;
    }

    public Set<ParcelaEntity> gerarDivisaoPersonalizada(DespesaEntity despesa,
                                                        List<ParcelaDTO> parcelasInformadas) throws BadRequestException, NotFoundException {
        //validação de erros
        validarDadosDaDespesa(despesa);
        if (parcelasInformadas.isEmpty()) {
            throw new BadRequestException("A lista de parcelas está vazia");
        }
        if (despesa.getTipoDivisao() != TipoDivisao.PERSONALIZADA) {
            throw new BadRequestException("o tipo de divisão da despesa não é Personalizada");
        }

        //criacao do Set
        Set<ParcelaEntity> parcelas = new HashSet<>();
        for (ParcelaDTO parcelaInformada : parcelasInformadas) {
            //consulta de pessoa dentro de parcelasInformadas
            Integer pessoaId = parcelaInformada.getPessoaId();
            PessoaEntity pessoa = pessoaRepository.findById(pessoaId)
                    .orElseThrow(() -> new NotFoundException("Pessoa não encontrada"));

            //montagem da entidade de parcela que sera adicionada ao set
            ParcelaEntity parcelaPessoa = new ParcelaEntity();
            parcelaPessoa.setPessoa(pessoa);
            parcelaPessoa.setValor_da_parcela(parcelaInformada
                    .getValor_da_parcela());
            parcelaPessoa.setDespesa(despesa);

            parcelas.add(parcelaPessoa);
        }

        validarParcelas(despesa, parcelas);
        return parcelas;
    }

    public void validarParcelas(
            DespesaEntity despesa,
            Set<ParcelaEntity> parcelas) throws BadRequestException {

        validarDadosDaDespesa(despesa);

        BigDecimal valorTotal = despesa.getValorTotal();

        if (valorTotal == null || valorTotal.signum() <= 0) {
            throw new BadRequestException(
                    "O valor total da despesa deve ser positivo"
            );
        }

        if (valorTotal.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException(
                    "O total deve ter no máximo duas casas decimais"
            );
        }

        if (parcelas == null || parcelas.isEmpty()) {
            throw new BadRequestException("A lista de parcelas está vazia");
        }

        Set<Integer> ids = new HashSet<>();
        BigDecimal soma = BigDecimal.ZERO;

        for (ParcelaEntity parcela : parcelas) {
            if (parcela == null
                    || parcela.getPessoa() == null
                    || parcela.getPessoa().getId() == null) {
                throw new BadRequestException(
                        "Cada parcela deve identificar uma pessoa cadastrada"
                );
            }

            Integer pessoaId = parcela.getPessoa().getId();

            if (!ids.add(pessoaId)) {
                throw new BadRequestException("A pessoa não pode ser adicionada duas vezes");
            }

            boolean pertenceAoGrupo = false;
            for (PessoaEntity participante : despesa.getGrupo().getPessoa()) {
                if (participante.getId().equals(pessoaId)) {
                    pertenceAoGrupo = true;
                    break;
                }

            }
            if (!pertenceAoGrupo) {
                throw new BadRequestException("A pessoa não pertence ao grupo da despesa");
            }

            BigDecimal valor = parcela.getValor_da_parcela();
            if (valor == null) {
                throw new BadRequestException(
                        "O valor da parcela é obrigatório"
                );
            }

            if (valor.stripTrailingZeros().scale() > 2) {
                throw new BadRequestException(
                        "A parcela deve ter no máximo duas casas decimais"
                );
            }

            if (valor.signum() < 0) {
                throw new BadRequestException(
                        "A parcela não pode ter valor negativo"
                );
            }

            if (despesa.getTipoDivisao() == TipoDivisao.PERSONALIZADA
                    && valor.signum() == 0) {
                throw new BadRequestException(
                        "Na divisão personalizada, a parcela deve ser positiva"
                );
            }

            soma = soma.add(valor);
        }

        if (soma.compareTo(valorTotal) != 0) {
            throw new BadRequestException(
                    "A soma das parcelas deve ser igual ao total da despesa"
            );
        }
    }

    private void validarDadosDaDespesa(DespesaEntity despesa)
            throws BadRequestException {

        if (despesa == null || despesa.getGrupo() == null) {
            throw new BadRequestException("Despesa e grupo são obrigatórios");
        }

        if (despesa.getTipoDivisao() == null) {
            throw new BadRequestException("O tipo de divisão é obrigatório");
        }

        BigDecimal total = despesa.getValorTotal();

        if (total == null || total.signum() <= 0) {
            throw new BadRequestException(
                    "O valor total da despesa deve ser positivo"
            );
        }

        if (total.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException(
                    "O total deve ter no máximo duas casas decimais"
            );
        }
    }
}

