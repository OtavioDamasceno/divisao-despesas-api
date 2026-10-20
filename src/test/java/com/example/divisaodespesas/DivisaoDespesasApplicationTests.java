package com.example.divisaodespesas;

import com.example.divisaodespesas.database.model.*;
import com.example.divisaodespesas.database.repository.*;
import com.example.divisaodespesas.dto.despesa.DespesaResponseDTO;
import com.example.divisaodespesas.service.DespesaService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Integra controllers, services, validacao e JPA usando apenas H2 em memoria.
 * Nao use @Transactional na classe: as requisicoes HTTP usam outra transacao.
 * Cada teste limpa exclusivamente o banco H2 configurado abaixo.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:primeiros-testes;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class DivisaoDespesasApplicationTests {
    @LocalServerPort int porta;
    @Autowired IPessoaRepository pessoas;
    @Autowired IGrupoRepository grupos;
    @Autowired IDespesaRepository despesas;
    @Autowired IParcelaRepository parcelas;
    @Autowired DespesaService despesaService;
    @Autowired TransactionTemplate transacao;
    @Autowired EntityManager entityManager;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private Integer anaId;
    private Integer brunoId;
    private Integer grupoId;

    @BeforeEach
    void prepararCenarioIndependente() {
        transacao.executeWithoutResult(status -> {
            entityManager.createNativeQuery("delete from grupo_pessoa").executeUpdate();
            parcelas.deleteAllInBatch();
            despesas.deleteAllInBatch();
            grupos.deleteAllInBatch();
            pessoas.deleteAllInBatch();
        });
        // IDs distintos tornam visivel a inversao pessoaId/grupoId no controller.
        novaPessoa("Reserva");
        anaId = novaPessoa("Ana");
        brunoId = novaPessoa("Bruno");
        grupoId = novoGrupo("Viagem");
    }

    @Test void cadastraPessoa() throws Exception {
        long antes = pessoas.count();
        status(201, chamar("POST", "/pessoa", "{\"nome\":\"Carla\",\"email\":\"carla@example.com\"}"));
        assertEquals(antes + 1, pessoas.count());
        assertTrue(pessoas.findByEmail("carla@example.com").isPresent());
    }

    @Test void rejeitaEmailDuplicado() throws Exception {
        String corpo = "{\"nome\":\"Carla\",\"email\":\"carla@example.com\"}";
        status(201, chamar("POST", "/pessoa", corpo));
        long antes = pessoas.count();
        status(400, chamar("POST", "/pessoa", corpo));
        assertEquals(antes, pessoas.count());
    }

    @Test void cadastraGrupoAberto() throws Exception {
        status(201, chamar("POST", "/grupos", "{\"nomeGrupo\":\"Novo\",\"moeda\":\"BRL\"}"));
        GrupoEntity novo = grupos.findAll().stream().filter(g -> g.getNomeGrupo().equals("Novo")).findFirst().orElseThrow();
        assertTrue(novo.isSituacao());
        assertEquals("BRL", novo.getMoeda());
    }

    @Test void rejeitaGrupoSemNome() throws Exception {
        long antes = grupos.count();
        status(400, chamar("POST", "/grupos", "{\"nomeGrupo\":\"   \",\"moeda\":\"BRL\"}"));
        assertEquals(antes, grupos.count());
    }

    @Test void rejeitaMoedaNaoSuportada() throws Exception {
        status(400, chamar("POST", "/grupos", "{\"nomeGrupo\":\"Novo\",\"moeda\":\"USD\"}"));
    }

    @Test void consultaGrupoEListaGrupos() throws Exception {
        HttpResponse<String> resposta = chamar("GET", "/grupos/" + grupoId, null);
        status(200, resposta);
        assertTrue(resposta.body().contains("Viagem"));
        status(200, chamar("GET", "/grupos", null));
        status(404, chamar("GET", "/grupos/2147483647", null));
    }

    @Test void adicionaPessoaComIdsDiferentes() throws Exception {
        assertNotEquals(grupoId, anaId);
        adicionar(anaId);
        assertTrue(participa(anaId));
        transacao.executeWithoutResult(s -> assertTrue(pessoas.findById(anaId).orElseThrow().getGrupo()
                .stream().anyMatch(g -> g.getId().equals(grupoId))));
    }

    @Test void rejeitaParticipacaoDuplicada() throws Exception {
        adicionar(anaId);
        status(400, chamar("POST", caminhoPessoa(anaId), null));
        assertTrue(participa(anaId));
    }

    @Test void removePessoaSemHistorico() throws Exception {
        adicionar(anaId);
        status(204, chamar("DELETE", caminhoPessoa(anaId), null));
        assertFalse(participa(anaId));
        assertTrue(pessoas.existsById(anaId));
    }

    @Test void rejeitaRemocaoDeNaoParticipante() throws Exception {
        status(400, chamar("DELETE", caminhoPessoa(anaId), null));
    }

    @Test void fechaGrupoVazioEImpedeNovasParticipacoes() throws Exception {
        status(204, chamar("PATCH", "/grupos/" + grupoId + "/fechamento", null));
        assertFalse(grupos.findById(grupoId).orElseThrow().isSituacao());
        status(400, chamar("POST", caminhoPessoa(anaId), null));
        status(400, chamar("PATCH", "/grupos/" + grupoId + "/fechamento", null));
    }

    @Test void divideCemReaisEntreTresPreservandoCentavos() throws Exception {
        Integer carlaId = novaPessoa("Carla");
        adicionar(anaId); adicionar(brunoId); adicionar(carlaId);
        status(201, chamar("POST", "/despesa", igualitaria("100.00", anaId, anaId, brunoId, carlaId)));
        DespesaResponseDTO dto = unicaDespesa();
        assertEquals(3, dto.getParcelas().size());
        BigDecimal soma = dto.getParcelas().stream().map(p -> p.getValor_da_parcela()).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, soma.compareTo(new BigDecimal("100.00")));
        Integer menorId = List.of(anaId, brunoId, carlaId).stream().min(Integer::compareTo).orElseThrow();
        dto.getParcelas().forEach(p -> assertEquals(0, p.getValor_da_parcela().compareTo(
                new BigDecimal(p.getPessoaId().equals(menorId) ? "33.34" : "33.33"))));
        assertEquals(3, parcelas.count());
    }

    @Test void permiteParcelaZeroQuandoHaMenosCentavosQuePessoas() throws Exception {
        adicionar(anaId); adicionar(brunoId);
        status(201, chamar("POST", "/despesa", igualitaria("0.01", anaId, anaId, brunoId)));
        assertEquals(2, unicaDespesa().getParcelas().size());
        assertTrue(unicaDespesa().getParcelas().stream().anyMatch(p -> p.getValor_da_parcela().signum() == 0));
    }

    @Test void rejeitaPagadorForaDoGrupoSemGravarDespesa() throws Exception {
        adicionar(brunoId);
        status(400, chamar("POST", "/despesa", igualitaria("100.00", anaId, brunoId)));
        nadaGravado();
    }

    @Test void rejeitaBeneficiarioForaDoGrupoSemGravarDespesa() throws Exception {
        adicionar(anaId);
        status(400, chamar("POST", "/despesa", igualitaria("100.00", anaId, anaId, brunoId)));
        nadaGravado();
    }

    @Test void rejeitaTotalNegativo() throws Exception {
        adicionar(anaId);
        status(400, chamar("POST", "/despesa", igualitaria("-1.00", anaId, anaId)));
        nadaGravado();
    }

    @Test void cadastraDivisaoPersonalizada() throws Exception {
        adicionar(anaId); adicionar(brunoId);
        status(201, chamar("POST", "/despesa", personalizada("100.00", anaId, "40.00", brunoId, "60.00")));
        DespesaResponseDTO dto = unicaDespesa();
        assertEquals(2, dto.getParcelas().size());
        dto.getParcelas().forEach(p -> assertEquals(0, p.getValor_da_parcela().compareTo(
                new BigDecimal(p.getPessoaId().equals(anaId) ? "40.00" : "60.00"))));
    }

    @Test void rejeitaSomaPersonalizadaDiferenteDoTotal() throws Exception {
        adicionar(anaId); adicionar(brunoId);
        status(400, chamar("POST", "/despesa", personalizada("100.00", anaId, "40.00", brunoId, "50.00")));
        nadaGravado();
    }

    @Test void rejeitaPessoaRepetidaNaDivisaoPersonalizada() throws Exception {
        adicionar(anaId);
        status(400, chamar("POST", "/despesa", personalizada("100.00", anaId, "40.00", anaId, "60.00")));
        nadaGravado();
    }

    @Test void impedeRemocaoDePagadorEBeneficiarioComHistorico() throws Exception {
        adicionar(anaId); adicionar(brunoId);
        status(201, chamar("POST", "/despesa", igualitaria("100.00", anaId, brunoId)));
        status(400, chamar("DELETE", caminhoPessoa(anaId), null));
        status(400, chamar("DELETE", caminhoPessoa(brunoId), null));
        assertTrue(participa(anaId)); assertTrue(participa(brunoId));
    }

    @Test void impedeFechamentoComSaldoPendente() throws Exception {
        adicionar(anaId); adicionar(brunoId);
        status(201, chamar("POST", "/despesa", igualitaria("100.00", anaId, anaId, brunoId)));
        status(400, chamar("PATCH", "/grupos/" + grupoId + "/fechamento", null));
        assertTrue(grupos.findById(grupoId).orElseThrow().isSituacao());
    }

    @Test void fechaGrupoComSaldosZerados() throws Exception {
        adicionar(anaId);
        status(201, chamar("POST", "/despesa", igualitaria("100.00", anaId, anaId)));
        status(204, chamar("PATCH", "/grupos/" + grupoId + "/fechamento", null));
        assertFalse(grupos.findById(grupoId).orElseThrow().isSituacao());
        status(400, chamar("POST", "/despesa", igualitaria("10.00", anaId, anaId)));
        assertEquals(1, despesas.count());
    }

    @Test void consultaDespesaPorIdGrupoEPagador() throws Exception {
        adicionar(anaId);
        status(201, chamar("POST", "/despesa", igualitaria("20.00", anaId, anaId)));
        Integer id = unicaDespesa().getId();
        for (String caminho : List.of("/despesa/" + id, "/despesa/grupo/" + grupoId, "/despesa/pagador/" + anaId)) {
            HttpResponse<String> resposta = chamar("GET", caminho, null);
            status(200, resposta);
            assertTrue(resposta.body().contains("Almoco"));
            assertTrue(resposta.body().contains("parcelas"));
        }
        status(404, chamar("GET", "/despesa/2147483647", null));
    }

    @Test void consultaParcelaPorId() throws Exception {
        adicionar(anaId);
        status(201, chamar("POST", "/despesa", igualitaria("20.00", anaId, anaId)));
        Integer id = unicaDespesa().getParcelas().iterator().next().getId();
        status(200, chamar("GET", "/parcela/" + id, null));
    }

    @Test void consultaParcelasPorPessoa() throws Exception {
        adicionar(anaId);
        status(201, chamar("POST", "/despesa", igualitaria("20.00", anaId, anaId)));
        status(200, chamar("GET", "/parcela/pessoa/" + anaId, null));
    }

    @Test void consultaParcelasPorDespesa() throws Exception {
        adicionar(anaId);
        status(201, chamar("POST", "/despesa", igualitaria("20.00", anaId, anaId)));
        status(200, chamar("GET", "/parcela/despesas/" + unicaDespesa().getId(), null));
    }

    private Integer novaPessoa(String nome) {
        PessoaEntity pessoa = new PessoaEntity();
        pessoa.setNome(nome);
        pessoa.setEmail(UUID.randomUUID() + "@example.com");
        return pessoas.save(pessoa).getId();
    }

    private Integer novoGrupo(String nome) {
        GrupoEntity grupo = new GrupoEntity();
        grupo.setNomeGrupo(nome); grupo.setMoeda("BRL"); grupo.setSituacao(true);
        return grupos.save(grupo).getId();
    }

    private void adicionar(Integer pessoaId) throws Exception {
        status(204, chamar("POST", caminhoPessoa(pessoaId), null));
    }

    private String caminhoPessoa(Integer pessoaId) { return "/grupos/" + grupoId + "/pessoas/" + pessoaId; }

    private boolean participa(Integer pessoaId) {
        return Boolean.TRUE.equals(transacao.execute(s -> grupos.findById(grupoId).orElseThrow().getPessoa()
                .stream().anyMatch(p -> p.getId().equals(pessoaId))));
    }

    private DespesaResponseDTO unicaDespesa() throws Exception {
        assertEquals(1, despesas.count());
        return despesaService.buscaPorId(despesas.findAll().get(0).getId());
    }

    private void nadaGravado() { assertEquals(0, despesas.count()); assertEquals(0, parcelas.count()); }

    private String igualitaria(String total, Integer pagador, Integer... beneficiarios) {
        String ids = java.util.Arrays.stream(beneficiarios).map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        return "{\"descricao\":\"Almoco\",\"valorTotal\":" + total + ",\"tipoDivisao\":\"IGUALITARIA\",\"grupoId\":"
                + grupoId + ",\"idPagador\":" + pagador + ",\"beneficiariosId\":[" + ids + "]}";
    }

    private String personalizada(String total, Integer pessoa1, String valor1, Integer pessoa2, String valor2) {
        return "{\"descricao\":\"Almoco\",\"valorTotal\":" + total + ",\"tipoDivisao\":\"PERSONALIZADA\",\"grupoId\":"
                + grupoId + ",\"idPagador\":" + anaId + ",\"parcelas\":[{\"pessoaId\":" + pessoa1
                + ",\"valor_da_parcela\":" + valor1 + "},{\"pessoaId\":" + pessoa2 + ",\"valor_da_parcela\":" + valor2 + "}]}";
    }

    private HttpResponse<String> chamar(String metodo, String caminho, String corpo) throws Exception {
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .timeout(java.time.Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpo))
                .build();
        return cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
    }

    private void status(int esperado, HttpResponse<String> resposta) {
        assertEquals(esperado, resposta.statusCode(), () -> resposta.uri() + "\nResposta: " + resposta.body());
    }
}
