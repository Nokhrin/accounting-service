package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;

import java.util.UUID;

public record TransferResult(
        UUID operationId,
        Account sourceAccountAfter,
        Account targetAccountAfter
) implements Result {
}
