package com.nokhrin.bank.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public record Transaction(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        UUID transactionId,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        TransactionType transactionType,

        @Column(nullable = false, precision = 16, scale = 2)
        BigDecimal amount,

        @Column
        UUID sourceAccountId,

        @Column
        UUID targetAccountId,

        @Column(nullable = false)
        Instant execTs,

        @Enumerated
        @Column(nullable = false)
        TransactionStatus transactionStatus
) {
}
