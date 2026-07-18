package com.nokhrin.accounting.domain.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface AccountEvent permits Deposited, Withdrawn, Transferred {
    UUID eventId();
    Instant occuredAt();
}
