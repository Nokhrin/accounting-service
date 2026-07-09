package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transfer(
        Account sourceAccount,
        Account targetAccount,
        BigDecimal amount
) implements Operation<TransferResult> {

    public TransferResult execute() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (sourceAccount.status() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot transfer from non-active account. Status: " + sourceAccount.status());
        }
        if (targetAccount.status() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot transfer to non-active account. Status: " + targetAccount.status());
        }
        if (sourceAccount.balance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds. Balance: " + sourceAccount.balance() + ", requested: " + amount);
        }

        Instant transferTs = Instant.now();

        Account sourceAccountAfter = new Account(
                sourceAccount.id(),
                sourceAccount.balance().subtract(amount),
                sourceAccount.status(),
                sourceAccount.holder(),
                sourceAccount.createdAt(),
                transferTs
        );

        Account targetAccountAfter = new Account(
                targetAccount.id(),
                targetAccount.balance().add(amount),
                targetAccount.status(),
                targetAccount.holder(),
                targetAccount.createdAt(),
                transferTs
        );

        return new TransferResult(
                UUID.randomUUID(),
                sourceAccountAfter,
                targetAccountAfter
        );
    }
}
