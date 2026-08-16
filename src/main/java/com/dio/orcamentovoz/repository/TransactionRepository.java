package com.dio.orcamentovoz.repository;

import com.dio.orcamentovoz.model.Transaction;
import com.dio.orcamentovoz.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findTop10ByOrderByCreatedAtDesc();

    List<Transaction> findByType(TransactionType type);
}
