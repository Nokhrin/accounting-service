package com.nokhrin.accounting.infrastructure.repository;

import com.nokhrin.accounting.domain.transaction.Transaction;
import com.nokhrin.accounting.domain.transaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("SELECT transaction FROM Transaction transaction WHERE transaction.sourceAccountId = :accountId OR transaction.targetAccountId = :accountId ORDER BY transaction.executionTimestamp DESC")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    List<Transaction> findByType(TransactionType type);

    List<Transaction> findByAccountIdAndPeriod(
            @Param("accountId") UUID accountId,
            @Param("timestampFrom") Instant from,
            @Param("timestampTo") Instant to
    );
}
