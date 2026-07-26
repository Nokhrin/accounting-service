package com.nokhrin.accounting.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AccountClosed(
        UUID eventId,
        UUID accountId,
        Instant executedAt,
        Instant recordedAt
) implements AccountEvent {
}
