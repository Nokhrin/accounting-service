package com.nokhrin.accounting.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AccountActivated(
        UUID eventId,
        UUID accountId,
        Instant executedAt,
        Instant recordedAt
) implements AccountEvent {
}
