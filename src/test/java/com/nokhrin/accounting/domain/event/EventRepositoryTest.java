package com.nokhrin.accounting.domain.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
class EventRepositoryTest {

    @Container
    static PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("test-db")
                    .withUsername("test-user")
                    .withPassword("test-password");

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }


    @Autowired
    private EventRepository eventRepository;

    @Test
    @Transactional
    void record_and_findByAccountId_eventPersisted() {
        UUID accountId = UUID.randomUUID();
        Instant executedAt = Instant.parse("2026-07-26T01:23:45Z");
        Instant recordedAt = Instant.parse("2026-07-27T12:34:56Z");

        AccountOpened event = new AccountOpened(
                UUID.randomUUID(),
                accountId,
                executedAt,
                recordedAt
        );

        eventRepository.record(event);

        List<Event> events = eventRepository.findByAccountId(accountId);

        assertEquals(1, events.size());

        AccountOpened found = (AccountOpened) events.getFirst();

        assertAll(
                () -> assertEquals(event.eventId(), found.eventId()),
                () -> assertEquals(accountId, found.accountId()),
                () -> assertEquals(executedAt, found.executedAt()),
                () -> assertEquals(recordedAt, found.recordedAt())
        );
    }
}