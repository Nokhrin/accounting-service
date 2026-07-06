package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;

import java.util.UUID;

public record TransferOperationResult(
        UUID operationId,
        Account sourceAfter,
        Account targetAfter
) implements OperationResult {
}
