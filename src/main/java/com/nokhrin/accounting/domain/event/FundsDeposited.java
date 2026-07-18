package com.nokhrin.accounting.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FundsDeposited(
        UUID id,
        UUID operationId,
        BigDecimal amount,
        UUID targetAccountId,
        Instant occurredAt
) implements OperationEvent {
}
