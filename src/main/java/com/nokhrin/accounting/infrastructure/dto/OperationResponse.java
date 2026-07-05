package com.nokhrin.accounting.infrastructure.dto;

import com.nokhrin.accounting.domain.operation.OperationStatus;
import com.nokhrin.accounting.domain.operation.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

public record OperationResponse(
        UUID operationId,
        UUID accountId,
        OperationType operationType,
        BigDecimal balanceUpdated,
        OperationStatus status
) {
}
