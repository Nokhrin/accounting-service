package com.nokhrin.accounting.domain.event;

import com.nokhrin.accounting.domain.operation.Initiator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DepositEvent(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        Instant eventTs,
        Initiator initiator
) implements Event {
}
