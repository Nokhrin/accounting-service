package com.nokhrin.accounting.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokhrin.accounting.domain.event.Event;
import com.nokhrin.accounting.domain.event.EventRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class JdbcEventRepository implements EventRepository {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public JdbcEventRepository(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(Event event) {

        String insertEventQuery = """
                INSERT INTO public.events
                (
                id,
                type,
                aggregate_id,
                occurred_at,
                payload,
                created_at
                )
                VALUES(?, ?, ?, ?, ?, ?);
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement= connection.prepareStatement(insertEventQuery)
                ) {
            preparedStatement.setObject(1, event.id());
            preparedStatement.setString(2, event.type());
            preparedStatement.setObject(3, event.aggregateId());
            preparedStatement.setTimestamp(4, Timestamp.from(event.occurredAt()));
            preparedStatement.setObject(5, objectMapper.writeValueAsString(event));
            preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save event", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event body", e);
        }
    }

    @Override
    public List<Event> findByAccountId(UUID accountId) {
        return List.of();
    }
}
