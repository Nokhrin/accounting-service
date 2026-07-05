package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record Deposit(
        Account targetAccount,
        BigDecimal amount
) implements Operation {

    public Account execute() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (targetAccount.status() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot deposit to non-active targetAccount. Status: " + targetAccount.status());
        }
        return new Account(
                targetAccount.id(),
                targetAccount.balance().add(amount),
                targetAccount.status(),
                targetAccount.createdAt(),
                Instant.now()
        );
    }

}
