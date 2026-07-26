package com.nokhrin.accounting.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokhrin.accounting.domain.event.*;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcEventRepository implements EventRepository {
    private final DataSource dataSource;
    private final ObjectMapper mapper;

    public JdbcEventRepository(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.mapper = objectMapper;
    }

    @Override
    public void append(Event event) {

        String insertEventQuery = """
                INSERT INTO public.events
                (id, type, aggregate_id, occurred_at, payload)
                VALUES(?, ?, ?, ?, ?);
                """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement= connection.prepareStatement(insertEventQuery)
                ) {
            preparedStatement.setObject(1, event.id());
            preparedStatement.setString(2, event.type());
            preparedStatement.setObject(3, event.aggregateId());
            preparedStatement.setTimestamp(4, Timestamp.from(event.occurredAt()));
            preparedStatement.setObject(5, mapper.writeValueAsString(event));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save event", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event body", e);
        }
    }

    @Override
    public List<Event> findByAccountId(UUID accountId) {
        String selectByIdQuery = """
                SELECT id, "type", aggregate_id, occurred_at, payload, created_at
                FROM public.events
                WHERE aggregate_id = ?;
                """;

        Connection connection=DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement=connection.prepareStatement(selectByIdQuery)){
            preparedStatement.setObject(1, accountId);

            try (ResultSet resultSet=preparedStatement.executeQuery()){
                List<Event> events=new ArrayList<>();
                while (resultSet.next()){
                    events.add(mapEvent(resultSet, mapper));
                }
                return events;
            }
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Event not found by account id: " + accountId,e);
        }
    }

    private Event mapEvent(ResultSet resultSet, ObjectMapper mapper) throws SQLException, JsonProcessingException {
        String type = resultSet.getString("type");
        String payload = resultSet.getString("payload");

        return switch (type){
            case "AccountOpened" -> mapper.readValue(payload, AccountOpened.class);
            case "AccountBlocked" -> mapper.readValue(payload, AccountBlocked.class);
            case "AccountActivated" -> mapper.readValue(payload, AccountActivated.class);
            case "AccountClosed" -> mapper.readValue(payload, AccountClosed.class);
            case "FundsDeposited" -> mapper.readValue(payload, FundsDeposited.class);
            case "FundsWithdrawn" -> mapper.readValue(payload, FundsWithdrawn.class);
            case "TransferCompleted" -> mapper.readValue(payload, TransferCompleted.class);
            default -> throw new IllegalStateException("Unexpected Event type: " + type);
        };
    }
}
