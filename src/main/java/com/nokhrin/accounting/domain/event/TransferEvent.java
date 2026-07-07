package com.nokhrin.accounting.domain.event;

import com.nokhrin.accounting.domain.operation.Initiator;

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
        Initiator initiator
) implements Event {
}