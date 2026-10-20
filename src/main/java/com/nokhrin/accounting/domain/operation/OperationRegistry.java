package com.nokhrin.accounting.domain.operation;

import java.util.UUID;

public interface OperationRegistry {
    boolean exists(UUID operationId);

    void register(UUID operationId, UUID accountId, String operationType);
}