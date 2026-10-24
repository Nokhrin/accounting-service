package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.operation.OperationRegistry;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class JdbcOperationRegistry implements OperationRegistry {
    @Override
    public boolean exists(UUID operationId) {
        return false;
    }

    @Override
    public void register(UUID operationId, UUID accountId, String operationType) {

    }
    // SELECT 1 FROM operations WHERE operation_id = ?
    // INSERT INTO operations (operation_id, account_id, operation_type) VALUES (?, ?, ?)
}