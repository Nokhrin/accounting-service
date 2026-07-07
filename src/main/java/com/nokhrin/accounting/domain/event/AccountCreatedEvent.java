package com.nokhrin.accounting.domain.event;

import com.nokhrin.accounting.domain.operation.Initiator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountCreatedEvent(
        UUID id,
        UUID accountId,
        BigDecimal balance,
        Instant ts,
        Initiator initiator
) implements Event {
}
