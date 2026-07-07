package com.nokhrin.accounting.infrastructure.dto;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.operation.Operation;
import com.nokhrin.accounting.domain.operation.OperationStatus;
import com.nokhrin.accounting.domain.operation.OperationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OperationResponse(
        UUID operationId,
        OperationStatus status,
        OperationType operationType,
        UUID primaryAccountId,
        BigDecimal primaryBalanceUpdated,
        UUID secondaryAccountId,
        BigDecimal secondaryBalanceUpdated
) {
    public static OperationResponse single(
            UUID operationId,
            OperationType operationType,
            Account account
    ) {
        return new OperationResponse(
                operationId,
                OperationStatus.COMPLETED,
                operationType,
                account.id(),
                account.balance(),
                null,
                null
        );
    }

    public static OperationResponse transfer(
            UUID operationId,
            OperationType operationType,
            Account primaryAccount,
            Account secondaryAccount
    ) {
        return new OperationResponse(
                operationId,
                OperationStatus.COMPLETED,
                operationType,
                primaryAccount.id(),
                primaryAccount.balance(),
                secondaryAccount.id(),
                secondaryAccount.balance()
        );
    }
}
