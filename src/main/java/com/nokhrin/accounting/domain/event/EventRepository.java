package com.nokhrin.accounting.domain.event;

import java.util.List;
import java.util.UUID;

public interface EventRepository {
    void append(Event event);
    List<Event> findByAccountId(UUID accountId);
}
