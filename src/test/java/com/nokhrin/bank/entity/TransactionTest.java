package com.nokhrin.bank.entity;

import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class TransactionTest {

    @Test
    public void testTransactionCreatedSuccess() {
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
                null, // sourceAccountId
                targetAccountId,
                execTs,
                status
        );

        assertEquals(transaction.transactionId(), expectedId);
        assertEquals(transaction.transactionType(), type);
        assertEquals(transaction.amount(), amount);
        assertNull(transaction.sourceAccountId());
        assertEquals(transaction.targetAccountId(), targetAccountId);
        assertEquals(transaction.execTs(), execTs);
        assertEquals(transaction.transactionStatus(), status);
    }

}