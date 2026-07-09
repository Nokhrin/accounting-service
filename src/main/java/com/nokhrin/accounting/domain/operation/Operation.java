package com.nokhrin.accounting.domain.operation;

public sealed interface Operation<R extends Result> permits Deposit, Withdrawal, Transfer {
    R execute();
}
