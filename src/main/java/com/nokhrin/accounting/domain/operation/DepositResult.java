package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;

import java.util.UUID;

public record DepositResult(
        UUID operationId,
        Account accountAfter
) implements Result {
}
