package com.nokhrin.accounting.domain.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface Event permits AccountEvent, OperationEvent
{
    UUID id();
    default String type(){
        return getClass().getSimpleName();
    }
    UUID aggregateId();
    Instant occurredAt();
}
