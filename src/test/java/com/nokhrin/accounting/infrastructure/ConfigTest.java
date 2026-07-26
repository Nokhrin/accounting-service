package com.nokhrin.accounting.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nokhrin.accounting.domain.event.FundsDeposited;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @Test
    void defaultObjectMapper_DeserializeInstantFailure() {
        ObjectMapper mapper = new ObjectMapper();

        String expectedTimestamp = "2026-07-26T01:23:45Z";
        FundsDeposited fundsDepositedEvent = new FundsDeposited(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100"),
                UUID.randomUUID(),
                Instant.parse(expectedTimestamp),
                Instant.parse("2026-07-27T12:34:56Z")
        );

        assertThrows(JsonProcessingException.class,
                () -> mapper.writeValueAsString(fundsDepositedEvent));
    }

    @Test
    void defaultObjectMapper_DeserializeInstantSuccess() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String expectedTimestamp = "2026-07-26T01:23:45Z";
        FundsDeposited fundsDepositedEvent = new FundsDeposited(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100"),
                UUID.randomUUID(),
                Instant.parse(expectedTimestamp),
                Instant.parse("2026-07-27T12:34:56Z")
        );

        try {
            String fundsDepositedSerialized = mapper.writeValueAsString(fundsDepositedEvent);
            FundsDeposited fundsDepositedDeserialized = mapper.readValue(fundsDepositedSerialized, FundsDeposited.class);

            assertAll(
                    () -> assertTrue(fundsDepositedSerialized.contains(expectedTimestamp)),
                    () -> assertEquals(fundsDepositedEvent.eventId(), fundsDepositedDeserialized.eventId()),
                    () -> assertEquals(fundsDepositedEvent.operationId(), fundsDepositedDeserialized.operationId()),
                    () -> assertEquals(fundsDepositedEvent.amount(), fundsDepositedDeserialized.amount()),
                    () -> assertEquals(fundsDepositedEvent.targetAccountId(), fundsDepositedDeserialized.targetAccountId()),
                    () -> assertEquals(fundsDepositedEvent.executedAt(), fundsDepositedDeserialized.executedAt()),
                    () -> assertEquals(fundsDepositedEvent.recordedAt(), fundsDepositedDeserialized.recordedAt())
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}