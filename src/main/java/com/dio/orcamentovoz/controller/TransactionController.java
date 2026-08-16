package com.dio.orcamentovoz.controller;

import com.dio.orcamentovoz.model.Transaction;
import com.dio.orcamentovoz.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints simples para inspecionar os dados sem depender da IA --
 * util para conferir, em testes, se o Tool Calling realmente persistiu algo.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> listarTodas() {
        return ResponseEntity.ok(transactionService.listarTodas());
    }

    @GetMapping("/saldo")
    public ResponseEntity<Map<String, Double>> saldo() {
        return ResponseEntity.ok(Map.of("saldo", transactionService.calcularSaldo()));
    }
}
