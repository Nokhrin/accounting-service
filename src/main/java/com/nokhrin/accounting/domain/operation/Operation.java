package com.nokhrin.accounting.domain.operation;

public sealed interface Operation permits Deposit, Withdraw, Transfer {
    OperationResult execute();
}
