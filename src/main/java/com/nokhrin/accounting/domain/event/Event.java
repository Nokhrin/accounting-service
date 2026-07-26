package com.nokhrin.accounting.domain.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface Event permits AccountEvent, OperationEvent
{
    UUID eventId();
    default String type(){
        return getClass().getSimpleName();
    }
    Instant executedAt();
    Instant recordedAt();
    UUID aggregateId();
}
