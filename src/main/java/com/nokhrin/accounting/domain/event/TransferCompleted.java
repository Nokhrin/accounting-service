package com.nokhrin.accounting.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCompleted(
        UUID id,
        UUID operationId,
        BigDecimal amount,
        UUID sourceAccountId,
        UUID targetAccountId,
        Instant occurredAt
) implements OperationEvent {
}
