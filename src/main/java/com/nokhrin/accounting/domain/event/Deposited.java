package com.nokhrin.accounting.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Deposited(
        UUID eventId,
        UUID accountId,
        UUID initiatorId,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        Instant occurredAt
) implements AccountEvent {
    @Override
    public Instant occuredAt() {
        return occurredAt;
    }
}
