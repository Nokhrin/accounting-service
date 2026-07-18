package com.nokhrin.accounting.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transferred(
        UUID eventId,
        UUID sourceAccountId,
        UUID targetAccountId,
        UUID initiatorId,
        BigDecimal amount,
        BigDecimal sourceBalanceBefore,
        BigDecimal sourceBalanceAfter,
        BigDecimal targetBalanceBefore,
        BigDecimal targetBalanceAfter,
        Instant occurredAt
) implements AccountEvent {
    @Override
    public Instant occuredAt() {
        return occurredAt;
    }
}
