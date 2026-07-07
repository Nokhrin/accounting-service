package com.nokhrin.accounting.domain.operation;

public sealed interface Operation<R extends OperationResult> permits Deposit, Withdraw, Transfer {
    R execute();
}
