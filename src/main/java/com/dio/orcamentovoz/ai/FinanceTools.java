package com.dio.orcamentovoz.ai;

import com.dio.orcamentovoz.model.Transaction;
import com.dio.orcamentovoz.model.TransactionType;
import com.dio.orcamentovoz.service.TransactionService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cada metodo aqui e uma "funcao real" que o modelo de linguagem pode decidir
 * chamar sozinho, de acordo com o comando de voz transcrito.
 * O Spring AI cuida de converter a decisao da IA em uma chamada Java de verdade.
 */
@Component
public class FinanceTools {

    private final TransactionService transactionService;

    public FinanceTools(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Tool(description = "Cria uma nova transacao financeira, que pode ser uma receita (dinheiro que entra) ou uma despesa (dinheiro que sai)")
    public String criarTransacao(
            @ToolParam(description = "descricao curta da transacao, por exemplo 'mercado' ou 'salario'") String descricao,
            @ToolParam(description = "valor numerico da transacao, sempre positivo") Double valor,
            @ToolParam(description = "tipo da transacao: RECEITA ou DESPESA") String tipo
    ) {
        try {
            TransactionType type = TransactionType.valueOf(tipo.trim().toUpperCase());
            Transaction transaction = transactionService.criar(descricao, valor, type);
            return "Transacao registrada com sucesso: %s de R$ %.2f (%s), id %d"
                    .formatted(transaction.getDescription(), transaction.getAmount(),
                            transaction.getType(), transaction.getId());
        } catch (IllegalArgumentException e) {
            return "Nao foi possivel registrar a transacao: " + e.getMessage();
        }
    }

    @Tool(description = "Consulta o saldo atual, somando todas as receitas e subtraindo todas as despesas")
    public String consultarSaldo() {
        double saldo = transactionService.calcularSaldo();
        return "O saldo atual e de R$ %.2f".formatted(saldo);
    }

    @Tool(description = "Lista as ultimas transacoes financeiras registradas, mais recentes primeiro")
    public String listarUltimasTransacoes() {
        List<Transaction> transacoes = transactionService.listarUltimas();
        if (transacoes.isEmpty()) {
            return "Ainda nao ha transacoes registradas.";
        }
        return transacoes.stream()
                .map(t -> "- %s: R$ %.2f (%s)".formatted(t.getDescription(), t.getAmount(), t.getType()))
                .collect(Collectors.joining("\n"));
    }
}
