package com.nokhrin.accounting.domain.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Account(
        UUID id,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt,
        Instant modifiedAt
) {

    public Account {
        if (balance == null) {
            throw new IllegalArgumentException("Balance must not be null");
        }
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Balance must be non-negative. Balance provided: " + balance);
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
    }

    public static Account create(BigDecimal initialBalance) {
        Instant creationTs = Instant.now();
        return new Account(
                UUID.randomUUID(),
                initialBalance,
                AccountStatus.ACTIVE,
                creationTs,
                creationTs
        );
    }

    public Account block() {
        if (status == AccountStatus.BLOCKED) {
            throw new IllegalStateException("Account " + id + " is already blocked");
        }
        return new Account(id, balance, AccountStatus.BLOCKED, createdAt, Instant.now());
    }

    public Account activate() {
        if (status != AccountStatus.BLOCKED) {
            throw new IllegalStateException("Can only activate blocked account. Current status: " + status);
        }
        return new Account(id, balance, AccountStatus.ACTIVE, createdAt, Instant.now());
    }

    public Account close() {
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account " + id + " is already closed");
        }
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Can close active account only. Current status: " + status);
        }
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance: " + balance);
        }
        return new Account(id, balance, AccountStatus.CLOSED, createdAt, Instant.now());
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean hasSufficientFunds(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
}