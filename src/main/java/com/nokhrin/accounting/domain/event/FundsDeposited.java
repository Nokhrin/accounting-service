package com.nokhrin.accounting.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FundsDeposited(
        UUID eventId,
        UUID operationId,
        BigDecimal amount,
        UUID targetAccountId,
        Instant executedAt,
        Instant recordedAt
) implements OperationEvent {
}
