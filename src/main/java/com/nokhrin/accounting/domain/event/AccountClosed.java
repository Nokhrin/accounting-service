package com.nokhrin.accounting.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AccountClosed(
        UUID id,
        UUID accountId,
        Instant occurredAt
) implements AccountEvent {
}
