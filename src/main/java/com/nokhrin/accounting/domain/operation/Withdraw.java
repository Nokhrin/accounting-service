package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Withdraw(
        Account sourceAccount,
        BigDecimal amount
) implements Operation {

    public OperationResult execute() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (sourceAccount.status() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot withdraw from non-active account. Status: " + sourceAccount.status());
        }
        if (sourceAccount.balance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds. Balance: " + sourceAccount.balance() + ", requested: " + amount);
        }
        Account sourceAccountAfter = new Account(
                sourceAccount.id(),
                sourceAccount.balance().subtract(amount),
                sourceAccount.status(),
                sourceAccount.createdAt(),
                Instant.now()
        );

        return new SingleOperationResult(
                UUID.randomUUID(),
                sourceAccountAfter
        );
    }
}
