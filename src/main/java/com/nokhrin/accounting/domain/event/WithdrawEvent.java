package com.nokhrin.accounting.domain.event;

import com.nokhrin.accounting.domain.shared.Actor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawEvent(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        Instant eventTs,
        Actor actor
) implements Event {
}
