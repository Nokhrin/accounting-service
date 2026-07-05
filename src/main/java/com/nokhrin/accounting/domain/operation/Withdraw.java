package com.nokhrin.accounting.domain.operation;

import java.math.BigDecimal;
import java.util.UUID;

public record Withdraw(
        UUID sourceId,
        BigDecimal amount
) implements Operation {
    /*

    public Account withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot withdraw to non-active targetAccount. Status: " + status);
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds. Balance: " + balance + ", requested: " + amount);
        }
        return new Account(id, balance.subtract(amount), status, createdAt, Instant.now());
    }

     */
}
