package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.operation.DuplicateOperationException;
import com.nokhrin.accounting.domain.operation.OperationRegistry;
import com.nokhrin.accounting.domain.operation.OperationType;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class JdbcOperationRegistry implements OperationRegistry {
    private static final String INSERT = """
            INSERT INTO operations (operation_id, account_id, operation_type)
            VALUES (?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcOperationRegistry(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void register(UUID operationId, UUID accountId, OperationType operationType) {
        try {
            jdbcTemplate.update(INSERT, operationId, accountId, operationType.name());
        } catch (DuplicateKeyException e) {
            throw new DuplicateOperationException(operationId);
        }
    }
}