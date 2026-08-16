package com.dio.orcamentovoz.service;

import com.dio.orcamentovoz.model.Transaction;
import com.dio.orcamentovoz.model.TransactionType;
import com.dio.orcamentovoz.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Regras de negocio das transacoes financeiras.
 * Tanto os endpoints REST quanto as ferramentas de IA (Tool Calling)
 * chamam este service -- nunca o repository diretamente.
 */
@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction criar(String description, Double amount, TransactionType type) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("O valor da transacao deve ser maior que zero.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A descricao da transacao e obrigatoria.");
        }

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setType(type);
        return repository.save(transaction);
    }

    public List<Transaction> listarTodas() {
        return repository.findAll();
    }

    public List<Transaction> listarUltimas() {
        return repository.findTop10ByOrderByCreatedAtDesc();
    }

    public double calcularSaldo() {
        double receitas = repository.findByType(TransactionType.RECEITA)
                .stream().mapToDouble(Transaction::getAmount).sum();
        double despesas = repository.findByType(TransactionType.DESPESA)
                .stream().mapToDouble(Transaction::getAmount).sum();
        return receitas - despesas;
    }
}
