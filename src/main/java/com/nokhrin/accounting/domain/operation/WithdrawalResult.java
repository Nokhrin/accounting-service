package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;

import java.util.UUID;

public record WithdrawalResult(
        UUID operationId,
        Account accountAfter
) implements Result {
}
