package com.example.divisaodespesas.service;

import com.example.divisaodespesas.database.model.DespesaEntity;
import com.example.divisaodespesas.database.model.GrupoEntity;
import com.example.divisaodespesas.database.model.ParcelaEntity;
import com.example.divisaodespesas.database.model.PessoaEntity;
import com.example.divisaodespesas.database.repository.IPessoaRepository;
import com.example.divisaodespesas.dto.PessoaDTO;
import com.example.divisaodespesas.exception.BadRequestException;
import com.example.divisaodespesas.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PessoaService {

    private static IPessoaRepository pessoaRepository;
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);


    public void criarPessoa(PessoaDTO pessoaDTO) throws BadRequestException {
        Matcher matcher = PATTERN.matcher(pessoaDTO.getEmail());
        if(!matcher.matches()){
            throw new BadRequestException("formato do email invalido");
        }
        PessoaEntity pessoa = pessoaRepository.findByEmail(pessoaDTO.getEmail())
                .orElse(null);
        if(pessoa != null){
            throw new BadRequestException("Essa pessoa ja foi cadastrada");
        }
        pessoaRepository.save(PessoaEntity.builder()
                        .nome(pessoaDTO.getNome())
                        .email(pessoaDTO.getEmail())
                .build());
    }

    public Set<ParcelaEntity> getParcelas(String email) throws NotFoundException {
        PessoaEntity pessoa = pessoaRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("pessoa nao encontrada!"));
        Set<ParcelaEntity> parcela = pessoa.getParcelas();
        return parcela;
    }
}
