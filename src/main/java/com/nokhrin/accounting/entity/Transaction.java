package com.nokhrin.accounting.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public record Transaction(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        UUID id,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        TransactionType type,

        @Column(nullable = false, precision = 16, scale = 2)
        BigDecimal amount,

        @Column
        UUID sourceAccountId,

        @Column
        UUID targetAccountId,

        @Column(nullable = false)
        Instant executionTimestamp,

        @Enumerated
        @Column(nullable = false)
        TransactionStatus status
) {
        public Transaction{
                if (id == null){
                        id =UUID.randomUUID();
                }
                if (executionTimestamp == null) {
                        executionTimestamp = Instant.now();
                }
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Amount must be positive. Amount provided: " + amount);
                }
        }
}
