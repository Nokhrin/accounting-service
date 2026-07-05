package com.nokhrin.accounting.domain.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        TransactionStatus status,
        UUID sourceAccountId,
        UUID targetAccountId,
        Instant executionTimestamp
) {
}
