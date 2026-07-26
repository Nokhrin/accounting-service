package com.nokhrin.accounting.domain.event;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventTest {
    @Test
    void type_returnSimpleClassName() {
        FundsDeposited fundsDepositedEvent = new FundsDeposited(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100"),
                UUID.randomUUID(),
                Instant.now(),
                Instant.now()
        );

        assertEquals("FundsDeposited", fundsDepositedEvent.type());
    }
}