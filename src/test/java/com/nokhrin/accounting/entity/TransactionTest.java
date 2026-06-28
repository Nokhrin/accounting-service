package com.nokhrin.accounting.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionTest {

    @Test
    void transaction_allFieldsMatch_expectedValues() {
        TransactionType type = TransactionType.DEPOSIT;
        BigDecimal amount = new BigDecimal("1500.00");
        UUID targetAccountId = UUID.randomUUID();
        Instant execTs = Instant.now();
        TransactionStatus status = TransactionStatus.COMPLETED;

        UUID expectedId = UUID.fromString("12345678-1234-1234-1234-123456789012");
        Transaction transaction = new Transaction(
                expectedId,
                type,
                amount,
                null,
                targetAccountId,
                execTs,
                status
        );

        assertEquals(expectedId, transaction.id());
        assertEquals(type, transaction.type());
        assertEquals(amount, transaction.amount());
        assertNull(transaction.sourceAccountId());
        assertEquals(targetAccountId, transaction.targetAccountId());
        assertEquals(execTs, transaction.executionTimestamp());
        assertEquals(status, transaction.status());
    }
}