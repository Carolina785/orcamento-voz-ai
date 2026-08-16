# Orçamento Voz AI

API de orçamento pessoal que entende comandos de voz usando IA (Spring AI + Groq) e executa ações reais de criação/consulta de transações financeiras.

## O que o projeto faz

Fluxo principal:

1. A pessoa envia um **áudio** com um comando (ex: "gastei 50 reais no mercado hoje").
2. O áudio é **transcrito em texto** (Whisper, via Spring AI).
3. O texto é enviado a um **modelo de linguagem**, que entende a intenção.
4. A IA decide qual **ferramenta (Tool Calling)** chamar: criar transação, consultar saldo ou listar últimas transações.
5. A ferramenta executa uma **função real** da aplicação, que persiste ou consulta dados no banco H2.
6. A IA gera uma **resposta final em texto**, devolvida para quem fez a requisição.

## Tecnologias usadas

- Java 17
- Spring Boot 3.3
- Spring AI 1.0 (ChatClient, Tool Calling, Audio Transcription) — usando o Groq como provedor de IA, via API compatível com OpenAI
- Spring Data JPA + H2 (banco em memória)
- Maven

## Como executar

### 1. Pré-requisitos
- Java 17+
- Uma chave de API do Groq (https://console.groq.com) — gratuita, sem necessidade de cartão de crédito
  
### 2. Configurar a chave da API
Este projeto usa o [Groq](https://console.groq.com) como provedor de IA — é gratuito e não exige cartão de crédito. A API do Groq é compatível com o formato da OpenAI, então o Spring AI funciona sem alterações no código, só na configuração.

1. Crie uma conta em console.groq.com
2. Gere uma chave em "API Keys"
3. Configure como variável de ambiente:

\`\`\`bash
export GROQ_API_KEY=sua-chave-aqui
\`\`\`

No IntelliJ: **Run > Edit Configurations > Modify options > Environment variables** e adicione `GROQ_API_KEY=sua-chave`.

### 3. Importar no IntelliJ
- Abra o IntelliJ → **Open** → selecione a pasta `orcamento-voz-ai`.
- O IntelliJ reconhece o `pom.xml` automaticamente e baixa as dependências via Maven.
- Rode a classe `OrcamentoVozApplication`.

### 4. Rodar por linha de comando (alternativa)
```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Como testar o fluxo principal

### Opção A — sem gravar áudio (mais simples para começar)
```bash
curl -X POST http://localhost:8080/api/voice-commands/texto \
  -H "Content-Type: application/json" \
  -d '{"comando": "gastei 50 reais no mercado"}'
```

### Opção B — com um arquivo de áudio real
```bash
curl -X POST http://localhost:8080/api/voice-commands \
  -F "audio=@caminho/para/comando.mp3"
```

Exemplos de comandos para testar:
- "Registre uma receita de 3000 reais referente ao salário"
- "Quanto eu gastei até agora, qual meu saldo?"
- "Quais foram minhas últimas transações?"

### Conferir os dados diretamente (sem IA)
```bash
curl http://localhost:8080/api/transactions
curl http://localhost:8080/api/transactions/saldo
```

Também é possível acessar o console do H2 em `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:orcamento`, usuário `sa`, senha em branco).

## Estrutura do projeto

```
model/        -> entidades JPA (Transaction, TransactionType)
repository/   -> acesso a dados (Spring Data JPA)
service/      -> regras de negócio (única camada que fala com o repository)
ai/           -> ferramentas expostas à IA via @Tool (Tool Calling)
controller/   -> endpoints REST (voz/texto e consulta direta)
dto/          -> objetos de request/response
```

A separação existe para que a IA nunca acesse o banco diretamente: ela sempre passa pelas
ferramentas em `ai/FinanceTools`, que por sua vez chamam o `TransactionService` — a mesma
camada usada pelos endpoints REST tradicionais.

## Próximos passos possíveis

- Adicionar geração de voz (text-to-speech) na resposta final.
- Criar novas ferramentas de consulta (ex: gastos por categoria, por período).
- Adicionar autenticação por usuário, separando as transações por conta.
- Escrever testes automatizados para o `TransactionService` e para `FinanceTools`.
