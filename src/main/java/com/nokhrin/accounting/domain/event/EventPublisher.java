package com.nokhrin.accounting.domain.event;

import java.util.UUID;

public interface EventPublisher {
    UUID publish();
}
