package com.nokhrin.accounting.domain.operation;

import java.util.UUID;

public interface OperationRegistry {
    void register(UUID operationId, UUID accountId, OperationType operationType);
}