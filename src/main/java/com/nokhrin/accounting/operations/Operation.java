package com.nokhrin.accounting.operations;

public sealed interface Operation permits Deposit, Withdraw, Transfer {
}
