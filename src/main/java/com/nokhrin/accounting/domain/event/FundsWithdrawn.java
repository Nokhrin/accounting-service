package com.nokhrin.accounting.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FundsWithdrawn(
        UUID eventId,
        UUID operationId,
        BigDecimal amount,
        UUID sourceAccountId,
        Instant executedAt,
        Instant recordedAt
) implements OperationEvent {
}
