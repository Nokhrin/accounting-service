package com.nokhrin.accounting.domain.event;

import com.nokhrin.accounting.domain.shared.Actor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferEvent(
        UUID id,
        BigDecimal amount,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal sourceBalanceBefore,
        BigDecimal sourceBalanceAfter,
        BigDecimal targetBalanceBefore,
        BigDecimal targetBalanceAfter,
        Instant eventTs,
        Actor actor
) implements Event {
}