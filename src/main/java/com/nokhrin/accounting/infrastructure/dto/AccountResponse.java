package com.nokhrin.accounting.infrastructure.dto;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt
) {
    public static AccountResponse from(Account account){
        return new AccountResponse(
                account.id(),
                account.balance(),
                account.status(),
                account.createdAt()
        );
    }
}
