# Divisão de Despesas API

API REST para organizar despesas compartilhadas em grupos, registrar quem pagou e distribuir o valor entre os participantes.

Projeto de estudo desenvolvido para praticar Java e Spring Boot, com foco em relacionamentos JPA, regras de negócio, validação de dados, DTOs, transações e testes de integração.

> **Status:** versão inicial em desenvolvimento. Cadastro, divisão de despesas e gerenciamento de participantes implementados. O registro de acertos entre pessoas ainda está pendente.

## Tecnologias

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- Jakarta Validation
- MySQL para execução da aplicação
- H2 em memória para testes
- JUnit Jupiter
- Lombok
- Maven Wrapper

## Funcionalidades

- Cadastrar pessoas e impedir e-mails duplicados.
- Criar grupos em reais (`BRL`), inicialmente abertos.
- Consultar e listar grupos.
- Adicionar participantes, impedindo participações duplicadas.
- Remover participantes sem histórico financeiro no grupo.
- Cadastrar despesas com um pagador e beneficiários.
- Dividir despesas de forma igualitária ou personalizada.
- Consultar despesas por ID, grupo e pagador.
- Consultar parcelas por ID, pessoa e despesa.
- Fechar grupos quando todos os participantes estiverem com saldo zerado.

## Modelo de domínio

| Entidade | Responsabilidade |
|---|---|
| Pessoa | Representa um participante, identificado por nome e e-mail. |
| Grupo | Reúne participantes e despesas; possui nome, moeda e situação. |
| Despesa | Registra descrição, valor total, tipo de divisão, grupo e pagador. |
| Parcela | Registra o valor atribuído a uma pessoa em uma despesa. |

Uma pessoa pode participar de vários grupos, e cada grupo pode ter várias pessoas. Cada despesa pertence a um grupo e possui um pagador. Suas parcelas vinculam os beneficiários aos valores que lhes cabem.

**Parcela, neste projeto, significa a parte de uma despesa atribuída a uma pessoa**, e não uma prestação de uma compra parcelada.

## Regras de negócio

- A moeda aceita nesta versão é `BRL`.
- O grupo começa aberto e pode ser criado sem participantes.
- O pagador e os beneficiários devem pertencer ao grupo da despesa.
- O pagador não precisa ser um dos beneficiários.
- O valor total da despesa deve ser positivo e ter no máximo duas casas decimais.
- Uma pessoa não pode aparecer duas vezes na divisão de uma mesma despesa.
- A soma das parcelas deve ser igual ao valor total.
- Grupos fechados não aceitam novas despesas nem inclusão ou remoção de participantes.
- Uma pessoa com despesas pagas ou parcelas no grupo não pode ser removida dele, mesmo que seu saldo esteja zerado.
- O cadastro da despesa e de suas parcelas ocorre na mesma transação.

### Divisão igualitária

O valor é dividido entre os beneficiários. Os centavos restantes são distribuídos em ordem crescente de ID, preservando o total.

Exemplo: R$ 100,00 para três pessoas resulta em **R$ 33,34 + R$ 33,33 + R$ 33,33**.

Quando há menos centavos do que beneficiários, algumas parcelas podem ser zero.

### Divisão personalizada

O usuário informa a pessoa e o valor de cada parcela. Todos os valores devem ser positivos, com no máximo duas casas decimais, e a soma deve corresponder ao total da despesa.

### Fechamento de grupo

Nesta versão, sem acertos entre pessoas, o saldo individual é calculado como:

```text
saldo = despesas pagas pela pessoa no grupo − parcelas atribuídas a ela no grupo
```

O fechamento exige saldo zero para **cada participante**. A soma dos saldos do grupo ser zero não é suficiente.

## Como executar

### Pré-requisitos

- JDK 17 configurado, incluindo `JAVA_HOME`.
- MySQL instalado e em execução.
- Git.
- Acesso à internet para baixar as dependências na primeira execução.

Não é necessário instalar o Maven separadamente: o projeto inclui Maven Wrapper.

### 1. Clonar o repositório

```bash
git clone https://github.com/OtavioDamasceno/divisao-despesas-api.git
cd divisao-despesas-api
```

### 2. Criar o banco

Execute no MySQL:

```sql
CREATE DATABASE divisao_despesas CHARACTER SET utf8mb4;
```

A aplicação usa `spring.jpa.hibernate.ddl-auto=update` para atualizar as tabelas durante o desenvolvimento. Essa configuração não substitui um processo de migração de banco em produção.

### 3. Configurar as variáveis de ambiente

| Variável | Exemplo |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/divisao_despesas` |
| `DATABASE_USERNAME` | Usuário com acesso ao banco |
| `DATABASE_PASSWORD` | Senha desse usuário |

No PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/divisao_despesas"
$env:DATABASE_USERNAME = "seu_usuario"
$env:DATABASE_PASSWORD = "sua_senha"
```

No Linux/macOS:

```bash
export DB_URL='jdbc:mysql://localhost:3306/divisao_despesas'
export DATABASE_USERNAME='seu_usuario'
export DATABASE_PASSWORD='sua_senha'
```

No IntelliJ, também é possível definir essas variáveis em **Run → Edit Configurations → Environment variables**, na configuração da aplicação.

As credenciais devem ser fornecidas pelo ambiente, sem serem adicionadas ao repositório.

### 4. Iniciar a aplicação

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

URL base: **`http://localhost:8082`**.

## Endpoints

As rotas abaixo refletem os nomes atuais do código, incluindo o uso de singular em `/pessoa`, `/despesa` e `/parcela`.

| Método | Rota | Operação | Sucesso |
|---|---|---|---|
| POST | `/pessoa` | Cadastrar pessoa | 201 |
| GET | `/pessoa/{email}/parcelas` | Consultar parcelas pelo e-mail¹ | 200 |
| POST | `/grupos` | Criar grupo | 201 |
| GET | `/grupos` | Listar grupos | 200 |
| GET | `/grupos/{grupoId}` | Consultar grupo | 200 |
| POST | `/grupos/{grupoId}/pessoas/{pessoaId}` | Adicionar participante | 204 |
| DELETE | `/grupos/{grupoId}/pessoas/{pessoaId}` | Remover participante | 204 |
| PATCH | `/grupos/{grupoId}/fechamento` | Fechar grupo | 204 |
| POST | `/despesa` | Criar despesa e gerar parcelas | 201 |
| GET | `/despesa/{id}` | Consultar despesa | 200 |
| GET | `/despesa/grupo/{grupoId}` | Listar despesas do grupo | 200 |
| GET | `/despesa/pagador/{id}` | Listar despesas pagas pela pessoa | 200 |
| GET | `/parcela/{parcelaId}` | Consultar parcela | 200 |
| GET | `/parcela/pessoa/{pessoaId}` | Listar parcelas da pessoa | 200 |
| GET | `/parcela/despesas/{despesaId}` | Listar parcelas da despesa | 200 |

¹ Essa rota ainda retorna entidades diretamente e precisa ser padronizada para DTOs. Para consultar parcelas, prefira a rota por ID da pessoa.

Os cadastros atualmente retornam `201` sem corpo. As operações com `204` também não retornam corpo.

Regras de negócio inválidas são tratadas com `400 Bad Request`; recursos não encontrados, com `404 Not Found`. As exceções de negócio possuem resposta com `message` e `status`. Erros de validação do framework podem ter formato diferente.

## Exemplos de requisições

Envie os corpos abaixo com `Content-Type: application/json`, usando Postman, Insomnia ou outro cliente HTTP.

Os IDs são ilustrativos: substitua-os pelos valores existentes no seu banco. Como o cadastro de pessoas ainda não devolve o ID nem existe uma rota de listagem de pessoas, é possível consultá-los no banco durante o desenvolvimento:

```sql
SELECT id, nome, email FROM pessoa;
```

Para obter os IDs dos grupos, use `GET /grupos`.

### Cadastrar pessoa

`POST /pessoa`

```json
{
  "nome": "Ana",
  "email": "ana@example.com"
}
```

### Criar grupo

`POST /grupos`

```json
{
  "nomeGrupo": "Viagem",
  "moeda": "BRL"
}
```

### Adicionar participantes

Após cadastrar as pessoas, associe-as ao grupo. Para grupo `1` e pessoas `1`, `2` e `3`:

```text
POST /grupos/1/pessoas/1
POST /grupos/1/pessoas/2
POST /grupos/1/pessoas/3
```

Essas requisições não precisam de corpo.

### Criar despesa igualitária

`POST /despesa`

```json
{
  "descricao": "Almoço da viagem",
  "valorTotal": 100.00,
  "tipoDivisao": "IGUALITARIA",
  "grupoId": 1,
  "idPagador": 1,
  "beneficiariosId": [1, 2, 3]
}
```

### Criar despesa personalizada

`POST /despesa`

```json
{
  "descricao": "Compras para a viagem",
  "valorTotal": 100.00,
  "tipoDivisao": "PERSONALIZADA",
  "grupoId": 1,
  "idPagador": 1,
  "parcelas": [
    {
      "pessoaId": 1,
      "valor_da_parcela": 40.00
    },
    {
      "pessoaId": 2,
      "valor_da_parcela": 60.00
    }
  ]
}
```

Não é necessário informar `despesaId` nas parcelas desse cadastro: o service faz a associação com a despesa criada.

Para visualizar o resultado, use `GET /despesa/grupo/1`.

## Testes de integração

A classe `DivisaoDespesasApplicationTests` contém **26 testes**, aprovados na execução local registrada durante o desenvolvimento.

Os testes iniciam a aplicação em uma porta aleatória, enviam requisições HTTP e utilizam repositories e services reais com banco H2 em memória. Cada cenário prepara seus próprios dados, sem utilizar o MySQL da aplicação.

Entre os cenários estão:

- Cadastro e validação de pessoas e grupos.
- Participação duplicada e remoção de participantes.
- Divisão igualitária e distribuição dos centavos.
- Divisão personalizada, soma incorreta e pessoa repetida.
- Pagador ou beneficiário fora do grupo.
- Consultas de despesas e parcelas.
- Proteção do histórico e fechamento dos grupos.

Execute com JDK 17:

```powershell
# Windows
.\mvnw.cmd -Dtest=DivisaoDespesasApplicationTests test
```

```bash
# Linux/macOS
./mvnw -Dtest=DivisaoDespesasApplicationTests test
```

No IntelliJ, execute a classe pelo botão ao lado do nome. Não é necessário iniciar a aplicação manualmente nem configurar as variáveis do MySQL para esses testes.

Os 26 cenários não representam cobertura total. Eles não comprovam diferenças específicas do MySQL, comportamento sob concorrência ou falhas de banco no meio de uma gravação.

## Organização do código

```text
src/
├── main/
│   ├── java/com/example/divisaodespesas/
│   │   ├── controller/
│   │   ├── database/
│   │   │   ├── model/
│   │   │   │   └── enums/
│   │   │   └── repository/
│   │   ├── dto/
│   │   │   ├── despesa/
│   │   │   ├── grupo/
│   │   │   ├── parcela/
│   │   │   └── pessoa/
│   │   ├── exception/
│   │   ├── handler/
│   │   └── service/
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/divisaodespesas/
        └── DivisaoDespesasApplicationTests.java
```

Controllers recebem as requisições; services executam as regras de negócio; repositories acessam o banco. Os DTOs representam os dados de entrada e saída, com padronizações ainda em andamento.

A versão atual não implementa autenticação ou autorização e foi construída para estudo e execução local.

## Autor

Desenvolvido por [Otávio Damasceno](https://github.com/OtavioDamasceno).
